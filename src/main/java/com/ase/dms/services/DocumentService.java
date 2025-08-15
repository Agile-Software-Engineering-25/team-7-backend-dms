package com.ase.dms.services;

import com.ase.dms.entities.DocumentEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public interface DocumentService {
  DocumentEntity createDocument(MultipartFile file, String folderId);
  DocumentEntity updateDocument(String id, DocumentEntity document);
  void deleteDocument(String id);
  DocumentEntity getDocument(String id);
  //Resource getDocumentFile(String id);
}
