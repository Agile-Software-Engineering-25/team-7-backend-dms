package com.ase.dms.services;

import com.ase.dms.entities.DocumentEntity;
import com.ase.dms.entities.FolderEntity;
import com.ase.dms.exceptions.DocumentNotFoundException;
import com.ase.dms.exceptions.DocumentUploadException;
import com.ase.dms.exceptions.FolderNotFoundException;
import com.ase.dms.helpers.NameIncrementHelper;
import com.ase.dms.repositories.DocumentRepository;
import com.ase.dms.repositories.FolderRepository;
import com.ase.dms.helpers.UuidValidator;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * Service implementation for document management.
 */
@Service
public class DocumentServiceImpl implements DocumentService {

  private final DocumentRepository documents;
  private final FolderRepository folders;

  /**
   * Constructor for DocumentServiceImpl.
   * @param documents the document repository
   * @param folders the folder repository
   */
  public DocumentServiceImpl(DocumentRepository documents, FolderRepository folders) {
    this.documents = documents;
    this.folders = folders;
  }

  /**
   * Create a new document in the given folder.
   * @param file the file to upload
   * @param folderId the target folder UUID
   * @return the created DocumentEntity
   */
  @Override
  @Transactional
  public DocumentEntity createDocument(MultipartFile file, String folderId) {
    UuidValidator.validateOrThrow(folderId);

    // Validate that folder exists and load it for relationship
    FolderEntity folder = folders.findById(folderId)
        .orElseThrow(() -> new FolderNotFoundException(folderId));

    try {
      DocumentEntity doc = new DocumentEntity();
      doc.setId(UUID.randomUUID().toString());

      // Set the folder relationship directly - cleaner approach
      doc.setFolder(folder);

      // Get siblings using JPA relationship
      List<DocumentEntity> siblings = folder.getDocuments();
      Set<String> siblingNames = NameIncrementHelper.collectSiblingNames(siblings, folderId, null);

      doc.setName(NameIncrementHelper.getIncrementedName(file.getOriginalFilename(), siblingNames));
      doc.setType(file.getContentType());
      doc.setSize(file.getSize());
      doc.setOwnerId("owner-id"); // später ersetzen, wenn es User gibt
      doc.setCreatedDate(LocalDateTime.now());
      doc.setDownloadUrl("/dms/v1/documents/" + doc.getId() + "/download");
      doc.setData(file.getBytes());

      return documents.save(doc);
    }
    catch (Exception e) {
      throw new DocumentUploadException(
          "Failed to process uploaded file: " + file.getOriginalFilename(), e);
    }
  }

  /**
   * Get a document by its ID.
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
   * @param id the document UUID
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

    if (incoming.getType() != null) {
      existing.setType(incoming.getType());
    }

    if (incoming.getFolderId() != null) {
      // Load the new folder and set the relationship directly
      FolderEntity newFolder = folders.findById(incoming.getFolderId())
          .orElseThrow(() -> new FolderNotFoundException(incoming.getFolderId()));
      existing.setFolder(newFolder);
    }

    if (incoming.getOwnerId() != null) {
      existing.setOwnerId(incoming.getOwnerId());
    }

    return documents.save(existing);
  }

  /**
   * Delete a document by its ID.
   * @param id the document UUID
   */
  @Override
  @Transactional
  public void deleteDocument(String id) {
    UuidValidator.validateOrThrow(id);
    if (!documents.existsById(id)) {
      throw new DocumentNotFoundException(id);
    }
    documents.deleteById(id);
  }
}
