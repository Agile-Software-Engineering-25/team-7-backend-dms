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
    folder.setId("ac917402-ba8c-48b9-8aa9-c2400c5833eb");
    folder.setName("Root");
    folder.setParentId("root");
    folder.setCreatedDate(LocalDateTime.now());

    FolderEntity sub = new FolderEntity();
    sub.setId("f2");
    sub.setName("Sub");
    sub.setParentId("ac917402-ba8c-48b9-8aa9-c2400c5833eb");
    sub.setCreatedDate(LocalDateTime.now());

    DocumentEntity doc = new DocumentEntity();
    doc.setId("d1");
    doc.setName("doc.txt");
    doc.setType("text/plain");
    doc.setSize(SIZE_10);
    doc.setFolderId("ac917402-ba8c-48b9-8aa9-c2400c5833eb");
    doc.setOwnerId("owner");
    doc.setCreatedDate(LocalDateTime.now());
    doc.setDownloadUrl("/dms/v1/documents/d1/download");
    doc.setData(new byte[0]);

    when(folderRepository.findById("ac917402-ba8c-48b9-8aa9-c2400c5833eb")).thenReturn(Optional.of(folder));
    when(folderRepository.findByParentId("ac917402-ba8c-48b9-8aa9-c2400c5833eb")).thenReturn(List.of(sub));
    when(documentRepository.findByFolderId("ac917402-ba8c-48b9-8aa9-c2400c5833eb")).thenReturn(List.of(doc));

    // Act
    FolderResponseDTO dto = folderService.getFolderContents("ac917402-ba8c-48b9-8aa9-c2400c5833eb");

    // Assert
    assertNotNull(dto);
    assertEquals("ac917402-ba8c-48b9-8aa9-c2400c5833eb", dto.getFolder().getId());
    assertEquals(1, dto.getSubfolders().size());
    assertEquals("f2", dto.getSubfolders().getFirst().getId());
    assertEquals(1, dto.getDocuments().size());
    assertEquals("d1", dto.getDocuments().getFirst().getId());
  }

  @Test
  void getFolderContents_nonExisting_throws() {
    when(folderRepository.findById("ac917402-ba8c-48b9-8aa9-c2400c5833eb")).thenReturn(Optional.empty());

    RuntimeException ex = assertThrows(RuntimeException.class,
        () -> folderService.getFolderContents("ac917402-ba8c-48b9-8aa9-c2400c5833eb"));
    assertTrue(ex.getMessage().contains("Ordner nicht gefunden"));
  }

  @Test
  void getFolderContents_withInvalidId_throwsFolderNotFoundException() {
    String invalidId = "not-a-uuid";
    FolderNotFoundException ex = assertThrows(FolderNotFoundException.class,
      () -> folderService.getFolderContents(invalidId));
    assertTrue(ex.getMessage().contains("Invalid folder id"));
  }

  @Test
  void getFolderContents_withRootId_alwaysReturnsFolder() {
    FolderEntity rootFolder = new FolderEntity();
    rootFolder.setId("root-uuid");
    rootFolder.setName("root");
    rootFolder.setParentId(null);
    rootFolder.setCreatedDate(LocalDateTime.now());

    when(folderRepository.findByNameAndParentIdIsNull("root")).thenReturn(Optional.of(rootFolder));
    when(folderRepository.findByParentId("root-uuid")).thenReturn(List.of());
    when(documentRepository.findByFolderId("root-uuid")).thenReturn(List.of());

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
    input.setParentId("root");
    input.setCreatedDate(LocalDateTime.now());

    // save soll zurückgeben, was reinkommt (mit gesetzter ID durch Service)
    when(folderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    FolderEntity created = folderService.createFolder(input);

    assertNotNull(created.getId());
    assertEquals("Neu", created.getName());
    verify(folderRepository, times(1)).save(any(FolderEntity.class));
  }

  @Test
  void updateFolder_existing_updatesName() {
    FolderEntity existing = new FolderEntity();
    existing.setId("f1");
    existing.setName("Alt");
    existing.setParentId("root");
    existing.setCreatedDate(LocalDateTime.now());

    when(folderRepository.findById(any())).thenReturn(Optional.of(existing));
    when(folderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    FolderEntity update = new FolderEntity();
    update.setName("Neu");

    FolderEntity result = folderService.updateFolder("f1", update);

    assertEquals("Neu", result.getName());
    assertEquals("f1", result.getId());
  }

  @Test
  void updateFolder_nonExisting_throws() {
    when(folderRepository.findById("nope")).thenReturn(Optional.empty());

    FolderEntity update = new FolderEntity();
    update.setName("X");

    RuntimeException ex = assertThrows(RuntimeException.class,
        () -> folderService.updateFolder("nope", update));
    assertTrue(ex.getMessage().contains("Ordner nicht gefunden"));
  }

  @Test
  void deleteFolder_existing_deletes() {
    when(folderRepository.existsById("f1")).thenReturn(true);

    folderService.deleteFolder("f1");

    verify(folderRepository, times(1)).deleteById("f1");
  }

  @Test
  void deleteFolder_nonExisting_throws() {
    when(folderRepository.existsById("nope")).thenReturn(false);

    RuntimeException ex = assertThrows(RuntimeException.class,
        () -> folderService.deleteFolder("nope"));
    assertTrue(ex.getMessage().contains("Ordner nicht gefunden"));
  }
}
