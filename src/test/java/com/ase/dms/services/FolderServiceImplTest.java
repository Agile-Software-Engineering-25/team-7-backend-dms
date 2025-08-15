package com.ase.dms.services;

import com.ase.dms.dtos.FolderResponseDTO;
import com.ase.dms.entities.FolderEntity;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FolderServiceImplTest {
  private FolderServiceImpl folderService;

  @BeforeEach
  void setUp() {
    folderService = new FolderServiceImpl();
  }

  @Test
  void testGetFolderContents_existingId_returnsContents() {
    FolderResponseDTO response = folderService.getFolderContents("test-id");
    assertNotNull(response);
    assertEquals("Dummy Folder", response.getFolder().getName());
    assertTrue(response.getSubfolders().isEmpty());
  }

  @Test
  void testGetFolderContents_nonExistingId_throwsException() {
    Exception exception = assertThrows(RuntimeException.class, () -> {
      folderService.getFolderContents("non-existing-id");
    });
    assertTrue(exception.getMessage().contains("Ordner nicht gefunden"));
  }

  @Test
  void testCreateFolder_addsFolderSuccessfully() {
    FolderEntity folder = new FolderEntity(null, "Neuer Ordner", "test-id", LocalDateTime.now());
    FolderEntity created = folderService.createFolder(folder);
    assertNotNull(created.getId());
    assertEquals("Neuer Ordner", created.getName());
    assertEquals("test-id", created.getParentId());
    assertEquals(created, folderService.getFolderContents(created.getId()).getFolder());
  }

  @Test
  void testUpdateFolder_updatesFolderSuccessfully() {
    FolderEntity original = folderService.getFolderContents("test-id").getFolder();
    FolderEntity updated = new FolderEntity(original.getId(), "Geänderter Name", original.getParentId(), original.getCreatedDate());
    FolderEntity result = folderService.updateFolder("test-id", updated);
    assertEquals("Geänderter Name", result.getName());
    assertEquals("test-id", result.getId());
  }

  @Test
  void testUpdateFolder_nonExistingId_throwsException() {
    FolderEntity dummy = new FolderEntity("non-id", "Dummy", "root", LocalDateTime.now());
    Exception exception = assertThrows(RuntimeException.class, () -> {
      folderService.updateFolder("non-id", dummy);
    });
    assertTrue(exception.getMessage().contains("Ordner nicht gefunden"));
  }

  @Test
  void testDeleteFolder_deletesFolderSuccessfully() {
    FolderEntity folder = new FolderEntity(null, "Zu löschen", "test-id", LocalDateTime.now());
    FolderEntity created = folderService.createFolder(folder);
    folderService.deleteFolder(created.getId());
    Exception exception = assertThrows(RuntimeException.class, () -> {
      folderService.getFolderContents(created.getId());
    });
    assertTrue(exception.getMessage().contains("Ordner nicht gefunden"));
  }

  @Test
  void testDeleteFolder_nonExistingId_throwsException() {
    Exception exception = assertThrows(RuntimeException.class, () -> {
      folderService.deleteFolder("non-existing-id");
    });
    assertTrue(exception.getMessage().contains("Ordner nicht gefunden"));
  }
}
