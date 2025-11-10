package com.ase.dms.services;

import com.ase.dms.entities.DocumentEntity;
import org.springframework.web.multipart.MultipartFile;

public interface DocumentService {
  DocumentEntity createDocument(MultipartFile file, String folderId, String[] tagUuids);
  DocumentEntity updateDocument(String id, DocumentEntity document);
  void deleteDocument(String id);
  DocumentEntity getDocument(String id);
  byte[] convertDocument(DocumentEntity document);
  DocumentEntity setDocumentTags(String id, String[] tags);
}
