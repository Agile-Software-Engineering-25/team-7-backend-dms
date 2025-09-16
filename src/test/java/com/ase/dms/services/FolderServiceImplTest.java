package com.ase.dms.services;

import com.ase.dms.dtos.FolderResponseDTO;
import com.ase.dms.entities.DocumentEntity;
import com.ase.dms.entities.FolderEntity;
import com.ase.dms.exceptions.FolderNotFoundException;
import com.ase.dms.repositories.DocumentRepository;
import com.ase.dms.repositories.FolderRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FolderServiceImplTest {
  private static final long SIZE_10 = 10L;

  @Mock
  private FolderRepository folderRepository;

  @Mock
  private DocumentRepository documentRepository;

  private FolderServiceImpl folderService; // <— DIESE Variable fehlte dir

  @BeforeEach
  void setUp() {
    folderService = new FolderServiceImpl(folderRepository, documentRepository);
  }

  @Test
  void getFolderContents_existingId_returnsFolderWithLists() {
    // Arrange
    FolderEntity folder = new FolderEntity();
    folder.setId("4111b676-474c-4014-a7ee-53fc5cb90127");
    folder.setName("Root");
    folder.setParentId("00000000-0000-0000-0000-000000000000");
    folder.setCreatedDate(LocalDateTime.now());

    FolderEntity sub = new FolderEntity();
    sub.setId("f2e1b676-474c-4014-a7ee-53fc5cb90127");
    sub.setName("Sub");
    sub.setParentId("4111b676-474c-4014-a7ee-53fc5cb90127");
    sub.setCreatedDate(LocalDateTime.now());

    DocumentEntity doc = new DocumentEntity();
    doc.setId("d1e1b676-474c-4014-a7ee-53fc5cb90127");
    doc.setName("doc.txt");
    doc.setType("text/plain");
    doc.setSize(SIZE_10);
    doc.setFolderId("4111b676-474c-4014-a7ee-53fc5cb90127");
    doc.setOwnerId("owner");
    doc.setCreatedDate(LocalDateTime.now());
    doc.setDownloadUrl("/dms/v1/documents/d1e1b676-474c-4014-a7ee-53fc5cb90127/download");
    doc.setData(new byte[0]);

    when(folderRepository.findById("4111b676-474c-4014-a7ee-53fc5cb90127")).thenReturn(Optional.of(folder));
    when(folderRepository.findByParentId("4111b676-474c-4014-a7ee-53fc5cb90127")).thenReturn(List.of(sub));
    when(documentRepository.findByFolderId("4111b676-474c-4014-a7ee-53fc5cb90127")).thenReturn(List.of(doc));

    // Act
    FolderResponseDTO dto = folderService.getFolderContents("4111b676-474c-4014-a7ee-53fc5cb90127");

    // Assert
    assertNotNull(dto);
    assertEquals("4111b676-474c-4014-a7ee-53fc5cb90127", dto.getFolder().getId());
    assertEquals(1, dto.getSubfolders().size());
    assertEquals("f2e1b676-474c-4014-a7ee-53fc5cb90127", dto.getSubfolders().getFirst().getId());
    assertEquals(1, dto.getDocuments().size());
    assertEquals("d1e1b676-474c-4014-a7ee-53fc5cb90127", dto.getDocuments().getFirst().getId());
  }

  @Test
  void getFolderContents_nonExisting_throws() {
    when(folderRepository.findById("4111b676-474c-4014-a7ee-53fc5cb90127")).thenReturn(Optional.empty());

    RuntimeException ex = assertThrows(RuntimeException.class,
        () -> folderService.getFolderContents("4111b676-474c-4014-a7ee-53fc5cb90127"));
    assertTrue(ex.getMessage().contains("Ordner 4111b676-474c-4014-a7ee-53fc5cb90127 nicht gefunden"));
  }

  @Test
  void getFolderContents_withInvalidId_throwsFolderNotFoundException() {
    String invalidId = "12345678-1234-1234-1234-1234567890ab"; // valid UUID, but not found
    when(folderRepository.findById(invalidId)).thenReturn(Optional.empty());
    FolderNotFoundException ex = assertThrows(FolderNotFoundException.class,
      () -> folderService.getFolderContents(invalidId));
    assertTrue(ex.getMessage().contains(invalidId));
  }

  @Test
  void getFolderContents_withRootId_alwaysReturnsFolder() {
    FolderEntity rootFolder = new FolderEntity();
    rootFolder.setId("00000000-0000-0000-0000-000000000000");
    rootFolder.setName("root");
    rootFolder.setParentId(null);
    rootFolder.setCreatedDate(LocalDateTime.now());

    when(folderRepository.findByNameAndParentIdIsNull("root")).thenReturn(Optional.of(rootFolder));
    when(folderRepository.findByParentId("00000000-0000-0000-0000-000000000000")).thenReturn(List.of());
    when(documentRepository.findByFolderId("00000000-0000-0000-0000-000000000000")).thenReturn(List.of());

    FolderResponseDTO response = folderService.getFolderContents("root");
    assertNotNull(response);
    assertNotNull(response.getFolder());
    assertEquals("root", response.getFolder().getName());
    assertNull(response.getFolder().getParentId());
  }

  @Test
  void createFolder_persistsAndReturns() {
    FolderEntity input = new FolderEntity();
    input.setName("Neu");
    input.setParentId("00000000-0000-0000-0000-000000000000");
    input.setCreatedDate(LocalDateTime.now());

    FolderEntity parent = new FolderEntity();
    parent.setId("00000000-0000-0000-0000-000000000000");
    parent.setName("root");
    parent.setParentId(null);
    parent.setCreatedDate(LocalDateTime.now());

    when(folderRepository.findById("00000000-0000-0000-0000-000000000000")).thenReturn(Optional.of(parent));
    when(folderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    FolderEntity created = folderService.createFolder(input);

    assertNotNull(created.getId());
    assertEquals("Neu", created.getName());
    verify(folderRepository, times(1)).save(any(FolderEntity.class));
  }

  @Test
  void updateFolder_existing_updatesName() {
    FolderEntity existing = new FolderEntity();
    existing.setId("f1e1b676-474c-4014-a7ee-53fc5cb90127");
    existing.setName("Alt");
    existing.setParentId("00000000-0000-0000-0000-000000000000");
    existing.setCreatedDate(LocalDateTime.now());

    when(folderRepository.findById(any())).thenReturn(Optional.of(existing));
    when(folderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    FolderEntity update = new FolderEntity();
    update.setName("Neu");

    FolderEntity result = folderService.updateFolder("f1e1b676-474c-4014-a7ee-53fc5cb90127", update);

    assertEquals("Neu", result.getName());
    assertEquals("f1e1b676-474c-4014-a7ee-53fc5cb90127", result.getId());
  }

  @Test
  void updateFolder_nonExisting_throws() {
    String nonExistingId = "12345678-1234-1234-1234-1234567890ab";
    when(folderRepository.findById(nonExistingId)).thenReturn(Optional.empty());

    FolderEntity update = new FolderEntity();
    update.setName("X");

    FolderNotFoundException ex = assertThrows(FolderNotFoundException.class,
        () -> folderService.updateFolder(nonExistingId, update));
    assertTrue(ex.getMessage().contains(nonExistingId));
  }

  @Test
  void deleteFolder_existing_deletes() {
    when(folderRepository.existsById("f1e1b676-474c-4014-a7ee-53fc5cb90127")).thenReturn(true);

    folderService.deleteFolder("f1e1b676-474c-4014-a7ee-53fc5cb90127");

    verify(folderRepository, times(1)).deleteById("f1e1b676-474c-4014-a7ee-53fc5cb90127");
  }

  @Test
  void deleteFolder_nonExisting_throws() {
    String nonExistingId = "12345678-1234-1234-1234-1234567890ab";
    when(folderRepository.existsById(nonExistingId)).thenReturn(false);

    FolderNotFoundException ex = assertThrows(FolderNotFoundException.class,
        () -> folderService.deleteFolder(nonExistingId));
    assertTrue(ex.getMessage().contains(nonExistingId));
  }
}
