package com.ase.dms.services;

import com.ase.dms.dtos.FolderResponseDTO;
import com.ase.dms.entities.DocumentEntity;
import com.ase.dms.entities.FolderEntity;
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
    folder.setId("f1");
    folder.setName("Root");
    folder.setParentId("root");
    folder.setCreatedDate(LocalDateTime.now());

    FolderEntity sub = new FolderEntity();
    sub.setId("f2");
    sub.setName("Sub");
    sub.setParentId("f1");
    sub.setCreatedDate(LocalDateTime.now());

    DocumentEntity doc = new DocumentEntity();
    doc.setId("d1");
    doc.setName("doc.txt");
    doc.setType("text/plain");
    doc.setSize(SIZE_10);
    doc.setFolderId("f1");
    doc.setOwnerId("owner");
    doc.setCreatedDate(LocalDateTime.now());
    doc.setDownloadUrl("/dms/v1/documents/d1/download");
    doc.setData(new byte[0]);

    when(folderRepository.findById("f1")).thenReturn(Optional.of(folder));
    when(folderRepository.findByParentId("f1")).thenReturn(List.of(sub));
    when(documentRepository.findByFolderId("f1")).thenReturn(List.of(doc));

    // Act
    FolderResponseDTO dto = folderService.getFolderContents("f1");

    // Assert
    assertNotNull(dto);
    assertEquals("f1", dto.getFolder().getId());
    assertEquals(1, dto.getSubfolders().size());
    assertEquals("f2", dto.getSubfolders().getFirst().getId());
    assertEquals(1, dto.getDocuments().size());
    assertEquals("d1", dto.getDocuments().getFirst().getId());
  }

  @Test
  void getFolderContents_nonExisting_throws() {
    when(folderRepository.findById("unknown")).thenReturn(Optional.empty());

    RuntimeException ex = assertThrows(RuntimeException.class,
        () -> folderService.getFolderContents("unknown"));
    assertTrue(ex.getMessage().contains("Ordner nicht gefunden"));
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
