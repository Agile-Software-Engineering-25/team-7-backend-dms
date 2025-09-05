package com.ase.dms.services;

import com.ase.dms.entities.DocumentEntity;
import com.ase.dms.helpers.NameIncrementHelper;
import com.ase.dms.repositories.DocumentRepository;
import com.ase.dms.helpers.UuidValidator;
import com.ase.dms.exceptions.DocumentNotFoundException;
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
    UuidValidator.validateOrThrow(folderId, new DocumentNotFoundException("Invalid folder id: must be UUID"));
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
    UuidValidator.validateOrThrow(id, new DocumentNotFoundException("Invalid document id: must be UUID"));
    return documents.findById(id)
      .orElseThrow(() -> new RuntimeException("Dokument nicht gefunden"));
  }

  @Override @Transactional
  public DocumentEntity updateDocument(String id, DocumentEntity incoming) {
    UuidValidator.validateOrThrow(id, new DocumentNotFoundException("Invalid document id: must be UUID"));
    DocumentEntity existing = getDocument(id);

    if (incoming.getName() != null) {
      // Bei Ordnerwechsel den neuen Ordner für Namenskonflikt-Prüfung verwenden
      String targetFolderId = incoming.getFolderId() != null ? incoming.getFolderId() : existing.getFolderId();
      UuidValidator.validateOrThrow(targetFolderId, new DocumentNotFoundException("Invalid folder id: must be UUID"));
      Set<String> siblingNames = NameIncrementHelper.collectSiblingNames(
          documents.findByFolderId(targetFolderId), targetFolderId, existing.getId());
      existing.setName(NameIncrementHelper.getIncrementedName(incoming.getName(), siblingNames));
    }

    if (incoming.getType() != null) {
      existing.setType(incoming.getType());
    }

    if (incoming.getFolderId() != null) {
      existing.setFolderId(incoming.getFolderId());
    }

    if (incoming.getOwnerId() != null) {
      existing.setOwnerId(incoming.getOwnerId());
    }

    return documents.save(existing);
  }

  @Override @Transactional
  public void deleteDocument(String id) {
    UuidValidator.validateOrThrow(id, new DocumentNotFoundException("Invalid document id: must be UUID"));
    if (!documents.existsById(id)){
      throw new RuntimeException("Dokument nicht gefunden");
    }
    documents.deleteById(id);
  }
}
