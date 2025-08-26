package com.ase.dms.services;

import com.ase.dms.entities.DocumentEntity;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

class DocumentServiceImplTest {
  private DocumentServiceImpl documentService;

  @BeforeEach
  void setUp() {
    documentService = new DocumentServiceImpl();
  }

  @Test
  void testGetDocument_existingId_returnsDocument() {
    DocumentEntity document = documentService.getDocument("test-id");
    assertNotNull(document);
    assertEquals("dummy.txt", document.getName());
    assertEquals("test-id", document.getId());
  }

  @Test
  void testGetDocument_nonExistingId_throwsException() {
    Exception exception = assertThrows(RuntimeException.class, () -> {
      documentService.getDocument("non-existing-id");
    });
    assertTrue(exception.getMessage().contains("Dokument nicht gefunden"));
  }

  @Test
  void testCreateDocument_addsDocumentSuccessfully() {
  MockMultipartFile file = new MockMultipartFile(
    "file",
    "testfile.txt",
    "text/plain",
    "Hello World".getBytes());
  DocumentEntity document = documentService.createDocument(
    file,
    "folder-123");
    assertNotNull(document);
    assertEquals("testfile.txt", document.getName());
    assertEquals("folder-123", document.getFolderId());
    assertEquals("text/plain", document.getType());
    assertNotNull(documentService.getDocument(document.getId()));
  }

  @Test
  void testCreateDocument_withNameConflict_incrementsName() {
    // Erstes Dokument mit gleichem Namen erstellen
    MockMultipartFile file1 = new MockMultipartFile(
      "file",
      "conflict.txt",
      "text/plain",
      "Hello".getBytes());
    DocumentEntity doc1 = documentService.createDocument(file1, "folder-1");
    assertEquals("conflict.txt", doc1.getName());

    // Zweites Dokument mit gleichem Namen erstellen
    MockMultipartFile file2 = new MockMultipartFile(
      "file",
      "conflict.txt",
      "text/plain",
      "World".getBytes());
    DocumentEntity doc2 = documentService.createDocument(file2, "folder-1");
    assertEquals("conflict.txt (1)", doc2.getName());

    // Drittes Dokument mit gleichem Namen erstellen
    MockMultipartFile file3 = new MockMultipartFile(
      "file",
      "conflict.txt",
      "text/plain",
      "Again".getBytes());
    DocumentEntity doc3 = documentService.createDocument(file3, "folder-1");
    assertEquals("conflict.txt (2)", doc3.getName());
  }

  @Test
  void testUpdateDocument_updatesDocumentSuccessfully() {
    DocumentEntity original = documentService.getDocument("test-id");
  DocumentEntity updated = new DocumentEntity(
    original.getId(),
    "updated.txt",
    original.getType(),
    original.getSize(),
    original.getFolderId(),
    original.getOwnerId(),
    original.getCreatedDate(),
    original.getDownloadUrl());
    DocumentEntity result = documentService.updateDocument("test-id", updated);
    assertEquals("updated.txt", result.getName());
    assertEquals("test-id", result.getId());
  }

  @Test
  void testUpdateDocument_withNameConflict_incrementsName() {
    // Dokumente mit gleichem Namen in einem Ordner
    MockMultipartFile file1 = new MockMultipartFile(
      "file",
      "update.txt",
      "text/plain",
      "Hello".getBytes());
    DocumentEntity doc1 = documentService.createDocument(file1, "folder-2");
    MockMultipartFile file2 = new MockMultipartFile(
      "file",
      "update.txt",
      "text/plain",
      "World".getBytes());
    DocumentEntity doc2 = documentService.createDocument(file2, "folder-2");
    // doc2 sollte update.txt(1) heißen
    assertEquals("update.txt (1)", doc2.getName());
    // doc1 auf den Namen von doc2 updaten
    doc1.setName("update.txt (1)");
    DocumentEntity updated = documentService.updateDocument(doc1.getId(), doc1);
    // Da update.txt(1) schon vergeben ist, sollte update.txt(2) entstehen
    assertEquals("update.txt (2)", updated.getName());
  }

  @Test
  void testUpdateDocument_nonExistingId_throwsException() {
    DocumentEntity dummy = new DocumentEntity(
      "non-id",
      "dummy.txt",
      "text/plain",
      1L,
      "folder",
      "owner",
      LocalDateTime.now(),
      "url"
    );
    Exception exception = assertThrows(RuntimeException.class, () -> {
      documentService.updateDocument("non-id", dummy);
    });
    assertTrue(exception.getMessage().contains("Dokument nicht gefunden"));
  }

  @Test
  void testDeleteDocument_deletesDocumentSuccessfully() {
    documentService.deleteDocument("test-id");
    Exception exception = assertThrows(RuntimeException.class, () -> {
      documentService.getDocument("test-id");
    });
    assertTrue(exception.getMessage().contains("Dokument nicht gefunden"));
  }

  @Test
  void testDeleteDocument_nonExistingId_throwsException() {
    Exception exception = assertThrows(RuntimeException.class, () -> {
      documentService.deleteDocument("non-existing-id");
    });
    assertTrue(exception.getMessage().contains("Dokument nicht gefunden"));
  }
}
