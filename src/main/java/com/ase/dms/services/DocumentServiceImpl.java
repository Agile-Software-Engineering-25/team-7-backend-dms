package com.ase.dms.services;

import com.ase.dms.entities.DocumentEntity;
import com.ase.dms.helpers.NameIncrementHelper;
import com.ase.dms.repositories.DocumentRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
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
      List<DocumentEntity> siblings = documents.findByFolderId(folderId);
      Set<String> siblingNames = NameIncrementHelper.collectSiblingNames(siblings, folderId, null);
      doc.setName(NameIncrementHelper.getIncrementedName(file.getOriginalFilename(), siblingNames));
      doc.setType(file.getContentType());
      doc.setSize(file.getSize());
      doc.setFolderId(folderId);
      doc.setOwnerId("owner-id"); // später ersetzen, wenn es User gibt
      doc.setCreatedDate(LocalDateTime.now());
      doc.setDownloadUrl("/dms/v1/documents/" + doc.getId() + "/download");
      doc.setData(file.getBytes()); // Dateiinhalt speichern
      return documents.save(doc);
    }
     catch (Exception e) {
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
    if (incoming.getName() != null){
      Set<String> siblingNames = NameIncrementHelper.collectSiblingNames(
          //TODO Should we check the existing folder or incoming folder?
          documents.findByFolderId(existing.getFolderId()), existing.getFolderId(), null);
      existing.setName(NameIncrementHelper.getIncrementedName(incoming.getName(), siblingNames));
    }
    // weitere Felder nach Bedarf
    return documents.save(existing);
  }

  @Override @Transactional
  public void deleteDocument(String id) {
    if (!documents.existsById(id)){
      throw new RuntimeException("Dokument nicht gefunden");
    }
    documents.deleteById(id);
  }
}
