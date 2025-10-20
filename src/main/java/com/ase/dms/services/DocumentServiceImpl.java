package com.ase.dms.services;

import com.ase.dms.dtos.DocumentEntityDTO;
import com.ase.dms.entities.DocumentEntity;
import com.ase.dms.entities.FolderEntity;
import com.ase.dms.exceptions.DocumentNotFoundException;
import com.ase.dms.exceptions.DocumentUploadException;
import com.ase.dms.exceptions.FolderNotFoundException;
import com.ase.dms.helpers.NameIncrementHelper;
import com.ase.dms.helpers.UuidValidator;
import com.ase.dms.repositories.DocumentRepository;
import com.ase.dms.repositories.FolderRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {

  private final DocumentRepository documents;
  private final FolderRepository folders;
  private final MinIOService minioService;

  @Override
  @Transactional
  public DocumentEntity createDocument(MultipartFile file, String folderId) {
    UuidValidator.validateOrThrow(folderId);

    FolderEntity folder = folders.findById(folderId)
        .orElseThrow(() -> new FolderNotFoundException(folderId));

    try {
      DocumentEntity doc = new DocumentEntity();
      doc.setId(UUID.randomUUID().toString());
      doc.setFolder(folder);

      List<DocumentEntity> siblings = folder.getDocuments();
      Set<String> siblingNames = NameIncrementHelper.collectSiblingNames(siblings, folderId, null);

      doc.setName(NameIncrementHelper.getIncrementedName(file.getOriginalFilename(), siblingNames));
      doc.setType(file.getContentType());
      doc.setOwnerId("owner-id"); // replace with actual ownerId later
      doc.setCreatedDate(LocalDateTime.now());
      doc.setDownloadUrl("/dms/v1/documents/" + doc.getId() + "/download");

      // Wrap in DTO to handle MinIO upload cleanly
      DocumentEntityDTO dto = new DocumentEntityDTO(doc, minioService);
      dto.setData(file.getBytes()); // uploads to MinIO and sets size internally

      // Save entity metadata with updated size
      return documents.save(dto.getDocumentEntity());

    } catch (Exception e) {
      throw new DocumentUploadException("Failed to process uploaded file: " + file.getOriginalFilename(), e);
    }
  }

  @Override
  public DocumentEntity getDocument(String id) {
    UuidValidator.validateOrThrow(id);
    return documents.findById(id)
        .orElseThrow(() -> new DocumentNotFoundException(id));
  }

  @Override
  @Transactional
  public DocumentEntity updateDocument(String id, DocumentEntity incoming) {
    UuidValidator.validateOrThrow(id);
    DocumentEntity existing = getDocument(id);
    DocumentEntityDTO dto = new DocumentEntityDTO(existing, minioService);

    if (incoming.getName() != null) {
      String targetFolderId = incoming.getFolderId() != null ? incoming.getFolderId() : existing.getFolderId();

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
      FolderEntity newFolder = folders.findById(incoming.getFolderId())
          .orElseThrow(() -> new FolderNotFoundException(incoming.getFolderId()));
      existing.setFolder(newFolder);
    }

    if (incoming.getOwnerId() != null) {
      existing.setOwnerId(incoming.getOwnerId());
    }

    // Handle possible binary update - if incoming has data (you may need a DTO for
    // this)
    // For this example, assume incoming.getData() is NOT in DocumentEntity, so
    // you'd pass binary data separately

    // Save updated metadata
    return documents.save(existing);
  }

  @Override
  @Transactional
  public void deleteDocument(String id) {
    UuidValidator.validateOrThrow(id);

    if (!documents.existsById(id)) {
      throw new DocumentNotFoundException(id);
    }

    // Delete file content from MinIO
    minioService.deleteObject(id);

    // Delete metadata record
    documents.deleteById(id);
  }
}
