package com.ase.dms.services;

import com.ase.dms.entities.DocumentEntity;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.mock.web.MockMultipartFile;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentServiceImplTest {

  private static final long SIZE_1_KB = 1024L;
  private static final long SIZE_100_B = 100L;

  @Mock
  private com.ase.dms.repositories.DocumentRepository documentRepository;

  private DocumentServiceImpl service;

  @BeforeEach
  void setUp() {
    service = new DocumentServiceImpl(documentRepository);
  }

  @Test
  void testGetDocument_existingId_returnsDocument() {
    // Arrange: baue ein Dokument mit SETTERN (inkl. data)
    DocumentEntity doc = new DocumentEntity();
    doc.setId("test-id");
    doc.setName("dummy.txt");
    doc.setType("text/plain");
    doc.setSize(SIZE_1_KB);
    doc.setFolderId("folder-1");
    doc.setOwnerId("owner-id");
    doc.setCreatedDate(LocalDateTime.now());
    doc.setDownloadUrl("/dms/v1/documents/test-id/download");
    doc.setData(new byte[0]);

    when(documentRepository.findById("test-id")).thenReturn(Optional.of(doc));

    // Act
    DocumentEntity result = service.getDocument("test-id");

    // Assert
    assertNotNull(result);
    assertEquals("dummy.txt", result.getName());
    assertEquals("test-id", result.getId());
  }

  @Test
  void testGetDocument_nonExistingId_throwsException() {
    when(documentRepository.findById("non-existing-id")).thenReturn(Optional.empty());

    Exception exception = assertThrows(RuntimeException.class, () -> service.getDocument("non-existing-id"));
    assertTrue(exception.getMessage().contains("Dokument nicht gefunden"));
  }

  @Test
  void testCreateDocument_addsDocumentSuccessfully() {
    // Arrange Upload-Datei
    MockMultipartFile file = new MockMultipartFile(
      "file", "testfile.txt", "text/plain", "Hello World".getBytes()
    );

    // save(...) soll genau das zurückgeben, was rein kommt
    when(documentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    // Act
    DocumentEntity created = service.createDocument(file, "folder-123");

    // Damit der folgende getDocument()-Aufruf klappt, stubben wir findById() mit der neuen ID:
    when(documentRepository.findById(created.getId())).thenReturn(Optional.of(created));

    // Assert
    assertNotNull(created);
    assertEquals("testfile.txt", created.getName());
    assertEquals("folder-123", created.getFolderId());
    assertEquals("text/plain", created.getType());


    assertNotNull(service.getDocument(created.getId()));
  }

  @Test
  void testCreateDocument_withNameConflict_incrementsName() {
    when(documentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    // Erstes Dokument: findByFolderId()() gibt leere Liste zurück
    when(documentRepository.findByFolderId(any())).thenReturn(List.of());

    MockMultipartFile file1 = new MockMultipartFile(
      "file",
      "conflict.txt",
      "text/plain",
      "Hello".getBytes());
    DocumentEntity doc1 = service.createDocument(file1, "folder-1");
    assertEquals("conflict.txt", doc1.getName());

    // Zweites Dokument: findByFolderId()() gibt doc1 zurück
    when(documentRepository.findByFolderId(any())).thenReturn(List.of(doc1));

    MockMultipartFile file2 = new MockMultipartFile(
      "file",
      "conflict.txt",
      "text/plain",
      "World".getBytes());
    DocumentEntity doc2 = service.createDocument(file2, "folder-1");
    assertEquals("conflict (1).txt", doc2.getName());

    // Drittes Dokument: findByFolderId()() gibt doc1 und doc2 zurück
    when(documentRepository.findByFolderId(any())).thenReturn(List.of(doc1, doc2));

    MockMultipartFile file3 = new MockMultipartFile(
      "file",
      "conflict.txt",
      "text/plain",
      "Again".getBytes());
    DocumentEntity doc3 = service.createDocument(file3, "folder-1");
    assertEquals("conflict (2).txt", doc3.getName());
  }

  @Test
  void testUpdateDocument_withNameConflict_incrementsName() {
    when(documentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
  // Erstes Dokument: keine vorhandenen Dokumente
  when(documentRepository.findByFolderId(any())).thenReturn(List.of());

    MockMultipartFile file1 = new MockMultipartFile(
      "file",
      "update.txt",
      "text/plain",
      "Hello".getBytes());
    DocumentEntity doc1 = service.createDocument(file1, "folder-2");

  // Zweites Dokument: doc1 ist bereits vorhanden
  when(documentRepository.findByFolderId(any())).thenReturn(List.of(doc1));

    MockMultipartFile file2 = new MockMultipartFile(
      "file",
      "update.txt",
      "text/plain",
      "World".getBytes());
    DocumentEntity doc2 = service.createDocument(file2, "folder-2");
    assertEquals("update (1).txt", doc2.getName());

  // Mock für findById() für updateDocument()
  when(documentRepository.findById(any())).thenReturn(Optional.of(doc1));

  // Update: beide Dokumente sind vorhanden
  when(documentRepository.findByFolderId("folder-2")).thenReturn(List.of(doc1, doc2));
    doc1.setName("update (1).txt");
    DocumentEntity updated = service.updateDocument(doc1.getId(), doc1);
    assertEquals("update (1) (1).txt", updated.getName());
  }

  @Test
  void testUpdateDocument_updatesDocumentSuccessfully() {
    // Arrange: vorhandenes Dokument
    DocumentEntity original = new DocumentEntity();
    original.setId("test-id");
    original.setName("dummy.txt");
    original.setType("text/plain");
    original.setSize(SIZE_100_B);
    original.setFolderId("folder-1");
    original.setOwnerId("owner-id");
    original.setCreatedDate(LocalDateTime.now());
    original.setDownloadUrl("/dms/v1/documents/test-id/download");
    original.setData(new byte[0]);

    when(documentRepository.findById("test-id")).thenReturn(Optional.of(original));
    when(documentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    // Update-Objekt nur mit geänderten Feldern
    DocumentEntity updated = new DocumentEntity();
    updated.setName("updated.txt");

    // Act
    DocumentEntity result = service.updateDocument("test-id", updated);

    // Assert
    assertEquals("updated.txt", result.getName());
    assertEquals("test-id", result.getId());
  }

  @Test
  void testUpdateDocument_nonExistingId_throwsException() {
    when(documentRepository.findById("non-id")).thenReturn(Optional.empty());

    DocumentEntity dummy = new DocumentEntity();
    dummy.setId("non-id");
    dummy.setName("dummy.txt");
    dummy.setType("text/plain");
    dummy.setSize(1L);
    dummy.setFolderId("folder");
    dummy.setOwnerId("owner");
    dummy.setCreatedDate(LocalDateTime.now());
    dummy.setDownloadUrl("url");
    dummy.setData(new byte[0]);

    Exception exception = assertThrows(RuntimeException.class, () -> service.updateDocument("non-id", dummy));
    assertTrue(exception.getMessage().contains("Dokument nicht gefunden"));
  }

  @Test
  void testDeleteDocument_deletesDocumentSuccessfully() {
    // existsById -> true, damit Delete erlaubt ist
    when(documentRepository.existsById("test-id")).thenReturn(true);
    // Nach dem Löschen simulieren wir "nicht mehr vorhanden"
    when(documentRepository.findById("test-id")).thenReturn(Optional.empty());

    // Act
    service.deleteDocument("test-id");

    // Assert: getDocument auf gelöschte ID wirft Exception
    Exception exception = assertThrows(RuntimeException.class, () -> service.getDocument("test-id"));
    assertTrue(exception.getMessage().contains("Dokument nicht gefunden"));

    verify(documentRepository).deleteById("test-id");
  }

  @Test
  void testDeleteDocument_nonExistingId_throwsException() {
    when(documentRepository.existsById("non-existing-id")).thenReturn(false);

    Exception exception = assertThrows(RuntimeException.class, () -> service.deleteDocument("non-existing-id"));
    assertTrue(exception.getMessage().contains("Dokument nicht gefunden"));
  }
}
