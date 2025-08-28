package com.ase.dms.services;

import com.ase.dms.entities.DocumentEntity;
import com.ase.dms.repositories.DocumentRepository;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class DocumentServiceImpl implements DocumentService {

  private final DocumentRepository documents;

  public DocumentServiceImpl(DocumentRepository documents) {
    this.documents = documents;
  }

  @Override @Transactional
  public DocumentEntity createDocument(MultipartFile file, String folderId) {
    try {
      DocumentEntity doc = new DocumentEntity();
      doc.setId(UUID.randomUUID().toString());
      doc.setName(file.getOriginalFilename());
      doc.setType(file.getContentType());
      doc.setSize(file.getSize());
      doc.setFolderId(folderId);
      doc.setOwnerId("owner-id"); // später ersetzen, wenn es User gibt
      doc.setCreatedDate(LocalDateTime.now());
      doc.setDownloadUrl("/dms/v1/documents/" + doc.getId() + "/download");
      doc.setData(file.getBytes()); // Dateiinhalt speichern
      return documents.save(doc);
    } catch (Exception e) {
      throw new RuntimeException("Upload fehlgeschlagen", e);
    }
  }

  @Override
  public DocumentEntity getDocument(String id) {
    return documents.findById(id)
      .orElseThrow(() -> new RuntimeException("Dokument nicht gefunden"));
  }

  @Override @Transactional
  public DocumentEntity updateDocument(String id, DocumentEntity incoming) {
    DocumentEntity existing = getDocument(id);
    if (incoming.getName() != null) existing.setName(incoming.getName());
    // weitere Felder nach Bedarf
    return documents.save(existing);
  }

  @Override @Transactional
  public void deleteDocument(String id) {
    if (!documents.existsById(id)) throw new RuntimeException("Dokument nicht gefunden");
    documents.deleteById(id);
  }
}
