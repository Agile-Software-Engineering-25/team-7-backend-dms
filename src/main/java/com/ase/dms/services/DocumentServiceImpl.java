package com.ase.dms.services;

import com.ase.dms.entities.DocumentEntity;
import com.ase.dms.entities.FolderEntity;
import com.ase.dms.exceptions.DocumentConversionException;
import com.ase.dms.exceptions.DocumentConversionInternalException;
import com.ase.dms.exceptions.DocumentNotFoundException;
import com.ase.dms.exceptions.DocumentUploadException;
import com.ase.dms.exceptions.FolderNotFoundException;
import com.ase.dms.exceptions.MinIOSetObjectDataException;
import com.ase.dms.exceptions.TagNotFoundException;
import com.ase.dms.helpers.NameIncrementHelper;
import com.ase.dms.helpers.UuidValidator;
import com.ase.dms.repositories.DocumentRepository;
import com.ase.dms.repositories.FolderRepository;
import com.ase.dms.security.UserInformationJWT;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jodconverter.core.DocumentConverter;
import org.jodconverter.core.document.DefaultDocumentFormatRegistry;
import org.jodconverter.core.document.DocumentFormat;
import org.jodconverter.core.document.DocumentFormatRegistry;
import org.jodconverter.core.office.OfficeException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 * Service implementation for document management.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {

  private final DocumentRepository documents;
  private final FolderRepository folders;

  private final MinIOService minIOService;
  private final DocumentConverter documentConverter;
  private final TagService tagService;

  /**
   * Create a new document in the given folder.
   *
   * @param file     the file to upload
   * @param folderId the target folder UUID
   * @return the created DocumentEntity
   */
  @Override
  @Transactional
  public DocumentEntity createDocument(MultipartFile file, String folderId, String[] tagUuids) {
    UuidValidator.validateOrThrow(folderId);

    // Validate that folder exists and load it for relationship
    FolderEntity folder = folders.findById(folderId)
        .orElseThrow(() -> new FolderNotFoundException(folderId));

    try {
      DocumentEntity doc = new DocumentEntity();
      doc.setId(UUID.randomUUID().toString());

      // Set the folder relationship directly - cleaner approach
      doc.setFolder(folder);
      doc.setTags(Arrays.stream(tagUuids).map(tagService::getTag).toList());

      // Get siblings using JPA relationship
      List<DocumentEntity> siblings = folder.getDocuments();
      Set<String> siblingNames = NameIncrementHelper.collectSiblingNames(siblings, folderId, null);

      doc.setName(NameIncrementHelper.getIncrementedName(file.getOriginalFilename(), siblingNames));
      doc.setType(file.getContentType());
      doc.setSize(file.getSize());
      doc.setOwnerId(UserInformationJWT.getUserId());
      doc.setCreatedDate(LocalDateTime.now());

      // Build download URL - use request context if available, otherwise use relative path
      String downloadUrl;
      try {
        downloadUrl = ServletUriComponentsBuilder.fromCurrentRequestUri()
            .replacePath(ServletUriComponentsBuilder.fromCurrentContextPath().build().getPath())
            .replaceQuery(null)
            .path("/v1/documents/")
            .path(doc.getId())
            .path("/download")
            .build()
            .toUriString();
      }
      catch (IllegalStateException e) {
        // No request context (e.g., in tests) - use relative path
        downloadUrl = "/dms/v1/documents/" + doc.getId() + "/download";
      }
      doc.setDownloadUrl(downloadUrl);

      minIOService.setObject(doc.getId(), file.getBytes());

      return documents.save(doc);
    }
    catch (TagNotFoundException | MinIOSetObjectDataException e) {
      throw e;
    }
    catch (Exception e) {
      throw new DocumentUploadException(
          "Failed to process uploaded file: " + file.getOriginalFilename(), e);
    }
  }

  /**
   * Get a document by its ID.
   *
   * @param id the document UUID
   * @return the DocumentEntity
   */
  @Override
  public DocumentEntity getDocument(String id) {
    UuidValidator.validateOrThrow(id);
    return documents.findById(id)
        .orElseThrow(() -> new DocumentNotFoundException(id));
  }

  /**
   * Update a document's metadata.
   *
   * @param id       the document UUID
   * @param incoming the new document data
   * @return the updated DocumentEntity
   */
  @Override
  @Transactional
  public DocumentEntity updateDocument(String id, DocumentEntity incoming) {
    UuidValidator.validateOrThrow(id);
    DocumentEntity existing = getDocument(id);

    if (incoming.getName() != null) {
      String targetFolderId = incoming.getFolderId() != null
          ? incoming.getFolderId()
          : existing.getFolderId();

      // Validate folder exists and get siblings via JPA relationship
      FolderEntity targetFolder = folders.findById(targetFolderId)
          .orElseThrow(() -> new FolderNotFoundException(targetFolderId));

      Set<String> siblingNames = NameIncrementHelper.collectSiblingNames(
          targetFolder.getDocuments(), targetFolderId, existing.getId());
      existing.setName(NameIncrementHelper.getIncrementedName(incoming.getName(), siblingNames));
    }

    if (incoming.getTags() != null) {
      existing.setTags(
          incoming.getTags().stream()
              .map(tag -> tagService.getTag(tag.getUuid()))
              .toList()
      );
    }

    if (incoming.getType() != null) {
      existing.setType(incoming.getType());
    }

    if (incoming.getFolderId() != null) {
      // Load the new folder and set the relationship directly
      FolderEntity newFolder = folders.findById(incoming.getFolderId())
          .orElseThrow(() -> new FolderNotFoundException(incoming.getFolderId()));
      existing.setFolder(newFolder);
    }

    return documents.save(existing);
  }

  /**
   * Delete a document by its ID.
   *
   * @param id the document UUID
   */
  @Override
  @Transactional
  public void deleteDocument(String id) {
    UuidValidator.validateOrThrow(id);
    if (!documents.existsById(id)) {
      throw new DocumentNotFoundException(id);
    }
    minIOService.deleteObject(id);
    documents.deleteById(id);
  }

  /**
   * Converts the document to pdf.
   *
   * @param document the document
   * @return the bytes of the converted document (pdf)
   */
  @Override
  @Transactional
  public byte[] convertDocument(DocumentEntity document) {
    byte[] data = minIOService.getObjectData(document.getId());

    // Determine if the document is an office document we can convert
    String type = document.getType() != null ? document.getType().toLowerCase(Locale.ROOT) : "";
    String name = document.getName() != null ? document.getName() : "document";

    // If already a PDF, just return it
    if (type.contains("pdf") || name.toLowerCase(Locale.ROOT).endsWith(".pdf")) {
      return data;
    }

    // Supported input types/extensions (common office types)
    boolean supported = type.contains("msword")
        || type.contains("officedocument")
        || type.contains("vnd.openxmlformats-officedocument")
        || name.toLowerCase(Locale.ROOT).matches(".*\\.(doc|docx|xls|xlsx|ppt|pptx)$");

    if (!supported) {
      throw new DocumentConversionException("Failed to convert unsupported type: " + type);
    }

    try (ByteArrayInputStream in = new ByteArrayInputStream(data);
         ByteArrayOutputStream out = new ByteArrayOutputStream()) {

      // Determine source and target document formats using the registry
      DocumentFormatRegistry registry = DefaultDocumentFormatRegistry.getInstance();

      // Try to resolve source format from MIME type first, then from file extension
      DocumentFormat sourceFormat = null;
      if (document.getType() != null && !document.getType().isEmpty()) {
        sourceFormat = registry.getFormatByMediaType(document.getType());
      }
      if (sourceFormat == null) {
        String ext = "";
        int dot = name.lastIndexOf('.');
        if (dot >= 0 && dot < name.length() - 1) {
          ext = name.substring(dot + 1).toLowerCase(Locale.ROOT);
        }
        if (!ext.isEmpty()) {
          sourceFormat = registry.getFormatByExtension(ext);
        }
      }

      if (sourceFormat == null) {
        throw new DocumentConversionException("Failed to resolve format of type: " + type);
      }

      DocumentFormat pdfFormat = registry.getFormatByMediaType("application/pdf");
      if (pdfFormat == null) {
        pdfFormat = registry.getFormatByExtension("pdf");
      }

      if (pdfFormat == null) {
        throw new DocumentConversionInternalException("Internal misconfigured conversion");
      }

      // Ensure pdfFormat is non-null for static analysis
      Objects.requireNonNull(pdfFormat, "PDF DocumentFormat not available");

      // Convert to PDF using jodconverter with resolved formats
      documentConverter.convert(in).as(sourceFormat).to(out).as(pdfFormat).execute();

      return out.toByteArray();
    }
    catch (OfficeException | IOException e) {
      throw new DocumentConversionException("Internal conversion error", e);
    }
  }

  @Override
  public DocumentEntity setDocumentTags(String id, String[] tags) {
    DocumentEntity doc = getDocument(id);
    log.info("Setting tags for document {} {}", id, tags);
    doc.setTags(Arrays.stream(tags).map(tagService::getTag).collect(Collectors.toCollection(ArrayList::new)));
    return documents.save(doc);
  }
}
