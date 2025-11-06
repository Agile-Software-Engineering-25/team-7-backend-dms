package com.ase.dms.services;

import com.ase.dms.entities.DocumentEntity;
import java.time.LocalDateTime;
import java.util.Optional;
import org.jodconverter.core.DocumentConverter;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

@ExtendWith(MockitoExtension.class)
class DocumentServiceImplTest {

  private static final long SIZE_1_KB = 1024L;
  private static final long SIZE_100_B = 100L;

  @Mock
  private com.ase.dms.repositories.DocumentRepository documentRepository;

  @Mock
  private com.ase.dms.repositories.FolderRepository folderRepository;

  @Mock
  private com.ase.dms.services.MinIOService minIOService;

  @Mock
  private DocumentConverter documentConverter;

  @Mock
  private TagServiceImpl tagService;

  private DocumentServiceImpl service;

  @BeforeEach
  void setUp() {
    service = new DocumentServiceImpl(documentRepository, folderRepository, minIOService, documentConverter, tagService);
  }

  @Test
  void testGetDocument_existingId_returnsDocument() {
    // Arrange: baue ein Dokument mit SETTERN (inkl. data)
    DocumentEntity doc = new DocumentEntity();
    doc.setId("4111b676-474c-4014-a7ee-53fc5cb90127");
    doc.setName("dummy.txt");
    doc.setType("text/plain");
    doc.setSize(SIZE_1_KB);
    doc.setFolderId("f1e1b676-474c-4014-a7ee-53fc5cb90127");
    doc.setOwnerId("owner-id");
    doc.setCreatedDate(LocalDateTime.now());
    doc.setDownloadUrl("/dms/v1/documents/4111b676-474c-4014-a7ee-53fc5cb90127/download");

    when(documentRepository.findById("4111b676-474c-4014-a7ee-53fc5cb90127")).thenReturn(Optional.of(doc));

    // Act
    DocumentEntity result = service.getDocument("4111b676-474c-4014-a7ee-53fc5cb90127");

    // Assert
    assertNotNull(result);
    assertEquals("dummy.txt", result.getName());
    assertEquals("4111b676-474c-4014-a7ee-53fc5cb90127", result.getId());
  }

  @Test
  void testGetDocument_nonExistingId_throwsException() {
    String nonExistingId = "12345678-1234-1234-1234-1234567890ab";
    when(documentRepository.findById(nonExistingId)).thenReturn(Optional.empty());

    RuntimeException exception = assertThrows(RuntimeException.class, () -> service.getDocument(nonExistingId));
    assertTrue(exception.getMessage().contains(nonExistingId));
  }

  @Test
  void testCreateDocument_addsDocumentSuccessfully() {
    // Arrange Upload-Datei
    MockMultipartFile file = new MockMultipartFile(
        "file", "testfile.txt", "text/plain", "Hello World".getBytes());

    // Mock the folder that the document will be created in
    com.ase.dms.entities.FolderEntity mockFolder = new com.ase.dms.entities.FolderEntity();
    mockFolder.setId("f1e1b676-474c-4014-a7ee-53fc5cb90127");
    mockFolder.setName("Test Folder");
    // Initialize empty documents list for JPA relationship
    mockFolder.setDocuments(new java.util.ArrayList<>());
    when(folderRepository.findById("f1e1b676-474c-4014-a7ee-53fc5cb90127")).thenReturn(Optional.of(mockFolder));

    // save(...) soll genau das zurückgeben, was rein kommt
    when(documentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    // Act
    DocumentEntity created = service.createDocument(file, "f1e1b676-474c-4014-a7ee-53fc5cb90127", new String[0]);

    // Damit der folgende getDocument()-Aufruf klappt, stubben wir findById() mit
    // der neuen ID:
    when(documentRepository.findById(created.getId())).thenReturn(Optional.of(created));

    // Assert
    assertNotNull(created);
    assertEquals("testfile.txt", created.getName());
    assertEquals("f1e1b676-474c-4014-a7ee-53fc5cb90127", created.getFolderId());
    assertEquals("text/plain", created.getType());

    assertNotNull(service.getDocument(created.getId()));
  }

  @Test
  void testCreateDocument_withNameConflict_incrementsName() {
    // Mock the folder that documents will be created in
    com.ase.dms.entities.FolderEntity mockFolder = new com.ase.dms.entities.FolderEntity();
    mockFolder.setId("f1e1b676-474c-4014-a7ee-53fc5cb90127");
    mockFolder.setName("Test Folder");

    when(documentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    // First document: empty folder
    mockFolder.setDocuments(new java.util.ArrayList<>());
    when(folderRepository.findById("f1e1b676-474c-4014-a7ee-53fc5cb90127")).thenReturn(Optional.of(mockFolder));

    MockMultipartFile file1 = new MockMultipartFile(
        "file",
        "conflict.txt",
        "text/plain",
        "Hello".getBytes());
    DocumentEntity doc1 = service.createDocument(file1, "f1e1b676-474c-4014-a7ee-53fc5cb90127", new String[0]);
    assertEquals("conflict.txt", doc1.getName());

    // Second document: folder now contains doc1
    mockFolder.setDocuments(java.util.List.of(doc1));
    when(folderRepository.findById("f1e1b676-474c-4014-a7ee-53fc5cb90127")).thenReturn(Optional.of(mockFolder));

    MockMultipartFile file2 = new MockMultipartFile(
        "file",
        "conflict.txt",
        "text/plain",
        "World".getBytes());
    DocumentEntity doc2 = service.createDocument(file2, "f1e1b676-474c-4014-a7ee-53fc5cb90127", new String[0]);
    assertEquals("conflict (1).txt", doc2.getName());

    // Third document: folder now contains doc1 and doc2
    mockFolder.setDocuments(java.util.List.of(doc1, doc2));
    when(folderRepository.findById("f1e1b676-474c-4014-a7ee-53fc5cb90127")).thenReturn(Optional.of(mockFolder));

    MockMultipartFile file3 = new MockMultipartFile(
        "file",
        "conflict.txt",
        "text/plain",
        "Again".getBytes());
    DocumentEntity doc3 = service.createDocument(file3, "f1e1b676-474c-4014-a7ee-53fc5cb90127", new String[0]);
    assertEquals("conflict (2).txt", doc3.getName());
  }

  @Test
  void testUpdateDocument_withNameConflict_incrementsName() {
    // Mock folder for both creation and update operations
    com.ase.dms.entities.FolderEntity mockFolder1 = new com.ase.dms.entities.FolderEntity();
    mockFolder1.setId("f2e1b676-474c-4014-a7ee-53fc5cb90127");
    mockFolder1.setName("Test Folder 1");

    when(documentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    // First document: empty folder
    mockFolder1.setDocuments(new java.util.ArrayList<>());
    when(folderRepository.findById("f2e1b676-474c-4014-a7ee-53fc5cb90127")).thenReturn(Optional.of(mockFolder1));

    MockMultipartFile file1 = new MockMultipartFile(
        "file",
        "update.txt",
        "text/plain",
        "Hello".getBytes());
    DocumentEntity doc1 = service.createDocument(file1, "f2e1b676-474c-4014-a7ee-53fc5cb90127", new String[0]);

    // Second document: folder now contains doc1
    mockFolder1.setDocuments(java.util.List.of(doc1));
    when(folderRepository.findById("f2e1b676-474c-4014-a7ee-53fc5cb90127")).thenReturn(Optional.of(mockFolder1));

    MockMultipartFile file2 = new MockMultipartFile(
        "file",
        "update.txt",
        "text/plain",
        "World".getBytes());
    DocumentEntity doc2 = service.createDocument(file2, "f2e1b676-474c-4014-a7ee-53fc5cb90127", new String[0]);
    assertEquals("update (1).txt", doc2.getName());

    // Mock für findById() für updateDocument()
    when(documentRepository.findById(any())).thenReturn(Optional.of(doc1));

    // Update: folder now contains both documents for name conflict resolution
    mockFolder1.setDocuments(java.util.List.of(doc1, doc2));
    when(folderRepository.findById("f2e1b676-474c-4014-a7ee-53fc5cb90127")).thenReturn(Optional.of(mockFolder1));

    doc1.setName("update (1).txt");
    DocumentEntity updated = service.updateDocument(doc1.getId(), doc1);
    assertEquals("update (1) (1).txt", updated.getName());
  }

  @Test
  void testUpdateDocument_updatesDocumentSuccessfully() {
    // Arrange: vorhandenes Dokument
    DocumentEntity original = new DocumentEntity();
    original.setId("4111b676-474c-4014-a7ee-53fc5cb90127");
    original.setName("dummy.txt");
    original.setType("text/plain");
    original.setSize(SIZE_100_B);
    original.setFolderId("f1e1b676-474c-4014-a7ee-53fc5cb90127");
    original.setOwnerId("owner-id");
    original.setCreatedDate(LocalDateTime.now());
    original.setDownloadUrl("/dms/v1/documents/4111b676-474c-4014-a7ee-53fc5cb90127/download");

    // Mock the folder for validation during update - initialize with empty
    // documents list
    com.ase.dms.entities.FolderEntity mockFolder = new com.ase.dms.entities.FolderEntity();
    mockFolder.setId("f1e1b676-474c-4014-a7ee-53fc5cb90127");
    mockFolder.setName("Test Folder");
    mockFolder.setDocuments(new java.util.ArrayList<>());
    when(folderRepository.findById("f1e1b676-474c-4014-a7ee-53fc5cb90127")).thenReturn(Optional.of(mockFolder));

    when(documentRepository.findById("4111b676-474c-4014-a7ee-53fc5cb90127")).thenReturn(Optional.of(original));
    when(documentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    // Update-Objekt nur mit geänderten Feldern
    DocumentEntity updated = new DocumentEntity();
    updated.setName("updated.txt");

    // Act
    DocumentEntity result = service.updateDocument("4111b676-474c-4014-a7ee-53fc5cb90127", updated);

    // Assert
    assertEquals("updated.txt", result.getName());
    assertEquals("4111b676-474c-4014-a7ee-53fc5cb90127", result.getId());
  }

  @Test
  void testUpdateDocument_nonExistingId_throwsException() {
    String nonId = "12345678-1234-1234-1234-1234567890ab";
    when(documentRepository.findById(nonId)).thenReturn(Optional.empty());

    DocumentEntity dummy = new DocumentEntity();
    dummy.setId(nonId);
    dummy.setName("dummy.txt");
    dummy.setType("text/plain");
    dummy.setSize(1L);
    dummy.setFolderId("f1e1b676-474c-4014-a7ee-53fc5cb90127");
    dummy.setOwnerId("owner");
    dummy.setCreatedDate(LocalDateTime.now());
    dummy.setDownloadUrl("url");

    RuntimeException exception = assertThrows(RuntimeException.class, () -> service.updateDocument(nonId, dummy));
    assertTrue(exception.getMessage().contains(nonId));
  }

  @Test
  void testDeleteDocument_deletesDocumentSuccessfully() {
    // existsById -> true, damit Delete erlaubt ist
    when(documentRepository.existsById("4111b676-474c-4014-a7ee-53fc5cb90127")).thenReturn(true);
    // Nach dem Löschen simulieren wir "nicht mehr vorhanden"
    when(documentRepository.findById("4111b676-474c-4014-a7ee-53fc5cb90127")).thenReturn(Optional.empty());

    // Act
    service.deleteDocument("4111b676-474c-4014-a7ee-53fc5cb90127");

    // Assert: getDocument auf gelöschte ID wirft Exception
    RuntimeException exception = assertThrows(RuntimeException.class,
        () -> service.getDocument("4111b676-474c-4014-a7ee-53fc5cb90127"));
    assertTrue(exception.getMessage().contains("4111b676-474c-4014-a7ee-53fc5cb90127"));

    verify(documentRepository).deleteById("4111b676-474c-4014-a7ee-53fc5cb90127");
  }

  @Test
  void testDeleteDocument_nonExistingId_throwsException() {
    String nonExistingId = "12345678-1234-1234-1234-1234567890ab";
    when(documentRepository.existsById(nonExistingId)).thenReturn(false);

    RuntimeException exception = assertThrows(RuntimeException.class, () -> service.deleteDocument(nonExistingId));
    assertTrue(exception.getMessage().contains(nonExistingId));
  }
}
