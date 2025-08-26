package com.ase.dms.services;

import com.ase.dms.entities.DocumentEntity;
import com.ase.dms.exceptions.DocumentNotFoundException;
import com.ase.dms.helpers.NameIncrementHelper;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class DocumentServiceImpl implements DocumentService {

  private static final long DUMMY_INITIAL_SIZE = 1024L;

  private final Map<String, DocumentEntity> documentStorage = new HashMap<>();

  public DocumentServiceImpl() { // Initializing with a dummy document for testing purposes
    String id = "test-id"; //UUID.randomUUID().toString();

    String downloadUrl = "/dms/v1/documents/" + id + "/download";
    DocumentEntity dummyDocument = new DocumentEntity(
        id,
        "dummy.txt",
        "text/plain",
        DUMMY_INITIAL_SIZE,
        "test-id",
        "owner-id",
        LocalDateTime.now(),
        downloadUrl);
    documentStorage.put(id, dummyDocument);
  }

  @Override
  public DocumentEntity getDocument(String id) {
    if (!documentStorage.containsKey(id)) {
      throw new DocumentNotFoundException("Dokument nicht gefunden");
    }
    return documentStorage.get(id);
  }

  @Override
  public DocumentEntity createDocument(MultipartFile file, String folderId) {
    Set<String> siblingNames = NameIncrementHelper.collectSiblingNames(documentStorage.values(), folderId, null);
    String uniqueName = NameIncrementHelper.getIncrementedName(file.getOriginalFilename(), siblingNames);
    String id = UUID.randomUUID().toString();
    String downloadUrl = "/dms/v1/documents/" + id + "/download";
    String ownerId = "owner-id"; // TODO: retrieve actual owner id
    DocumentEntity document = new DocumentEntity(
        id,
        uniqueName,
        file.getContentType(),
        file.getSize(),
        folderId,
        ownerId,
        LocalDateTime.now(),
        downloadUrl);
    documentStorage.put(id, document);
    return document;
  }

  @Override
  public DocumentEntity updateDocument(String id, DocumentEntity document) {
    if (!documentStorage.containsKey(id)) {
      throw new DocumentNotFoundException("Dokument nicht gefunden");
    }
    Set<String> siblingNames = NameIncrementHelper.collectSiblingNames(documentStorage.values(), document.getFolderId(), id);
    String uniqueName = NameIncrementHelper.getIncrementedName(document.getName(), siblingNames);
    document.setName(uniqueName);
    document.setId(id);
    documentStorage.put(id, document);
    return document;
  }

  @Override
  public void deleteDocument(String id) {
    if (!documentStorage.containsKey(id)) {
            throw new DocumentNotFoundException("Dokument nicht gefunden");
    }
    documentStorage.remove(id);
  }
}
