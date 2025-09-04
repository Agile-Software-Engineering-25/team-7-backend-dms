package com.ase.dms.helpers;

import com.ase.dms.entities.DocumentEntity;
import com.ase.dms.entities.FolderEntity;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class NameIncrementHelperTest {
  @Test
  void testGetIncrementedName_noConflict() {
    Set<String> names = new HashSet<>(Arrays.asList("file.txt", "other.txt"));
    String result = NameIncrementHelper.getIncrementedName("new.txt", names);
    assertEquals("new.txt", result);
  }

  @Test
  void testGetIncrementedName_withConflict() {
    Set<String> names = new HashSet<>(Arrays.asList("file", "file (1)", "file (2)"));
    String result = NameIncrementHelper.getIncrementedName("file", names);
    assertEquals("file (3)", result);
  }

  @Test
  void testCollectSiblingNames_folders() {
    FolderEntity f1 = new FolderEntity("1", "A", "root", LocalDateTime.now());
    FolderEntity f2 = new FolderEntity("2", "B", "root", LocalDateTime.now());
    FolderEntity f3 = new FolderEntity("3", "C", "other", LocalDateTime.now());
    List<FolderEntity> folders = Arrays.asList(f1, f2, f3);
    Set<String> names = NameIncrementHelper.collectSiblingNames(folders, "root", null);
    assertTrue(names.contains("A"));
    assertTrue(names.contains("B"));
    assertFalse(names.contains("C"));
  }

  @Test
  void testCollectSiblingNames_documents() {
    long size = 1024L;
    DocumentEntity d1 = new DocumentEntity(
        "1", "doc.txt", "text/plain", size, "folder1", "owner", LocalDateTime.now(), "url", new byte[0]);
    DocumentEntity d2 = new DocumentEntity(
        "2", "doc2.txt", "text/plain", size, "folder1", "owner", LocalDateTime.now(), "url", new byte[0]);
    DocumentEntity d3 = new DocumentEntity(
        "3", "other.txt", "text/plain", size, "folder2", "owner", LocalDateTime.now(), "url", new byte[0]);
    List<DocumentEntity> docs = Arrays.asList(d1, d2, d3);
    Set<String> names = NameIncrementHelper.collectSiblingNames(docs, "folder1", null);
    assertTrue(names.contains("doc.txt"));
    assertTrue(names.contains("doc2.txt"));
    assertFalse(names.contains("other.txt"));
  }

  @Test
  void testCollectSiblingNames_excludeId() {
    FolderEntity f1 = new FolderEntity("1", "A", "root", LocalDateTime.now());
    FolderEntity f2 = new FolderEntity("2", "B", "root", LocalDateTime.now());
    List<FolderEntity> folders = Arrays.asList(f1, f2);
    Set<String> names = NameIncrementHelper.collectSiblingNames(folders, "root", "1");
    assertFalse(names.contains("A"));
    assertTrue(names.contains("B"));
  }
}
