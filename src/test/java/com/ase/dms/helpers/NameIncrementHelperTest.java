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
  private static final long TEST_SIZE = 1024L;

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
    FolderEntity f1 = new FolderEntity();
    f1.setId("1");
    f1.setName("A");
    f1.setParentId("root");
    f1.setCreatedDate(LocalDateTime.now());

    FolderEntity f2 = new FolderEntity();
    f2.setId("2");
    f2.setName("B");
    f2.setParentId("root");
    f2.setCreatedDate(LocalDateTime.now());

    FolderEntity f3 = new FolderEntity();
    f3.setId("3");
    f3.setName("C");
    f3.setParentId("other");
    f3.setCreatedDate(LocalDateTime.now());

    List<FolderEntity> folders = Arrays.asList(f1, f2, f3);
    Set<String> names = NameIncrementHelper.collectSiblingNames(folders, "root", null);
    assertTrue(names.contains("A"));
    assertTrue(names.contains("B"));
    assertFalse(names.contains("C"));
  }

  @Test
  void testCollectSiblingNames_documents() {
    DocumentEntity d1 = new DocumentEntity();
    d1.setId("1");
    d1.setName("doc.txt");
    d1.setType("text/plain");
    d1.setSize(TEST_SIZE);
    d1.setFolderId("folder1");
    d1.setOwnerId("owner");
    d1.setCreatedDate(LocalDateTime.now());
    d1.setDownloadUrl("url");

    DocumentEntity d2 = new DocumentEntity();
    d2.setId("2");
    d2.setName("doc2.txt");
    d2.setType("text/plain");
    d2.setSize(TEST_SIZE);
    d2.setFolderId("folder1");
    d2.setOwnerId("owner");
    d2.setCreatedDate(LocalDateTime.now());
    d2.setDownloadUrl("url");

    DocumentEntity d3 = new DocumentEntity();
    d3.setId("3");
    d3.setName("other.txt");
    d3.setType("text/plain");
    d3.setSize(TEST_SIZE);
    d3.setFolderId("folder2");
    d3.setOwnerId("owner");
    d3.setCreatedDate(LocalDateTime.now());
    d3.setDownloadUrl("url");

    List<DocumentEntity> docs = Arrays.asList(d1, d2, d3);
    Set<String> names = NameIncrementHelper.collectSiblingNames(docs, "folder1", null);
    assertTrue(names.contains("doc.txt"));
    assertTrue(names.contains("doc2.txt"));
    assertFalse(names.contains("other.txt"));
  }

  @Test
  void testCollectSiblingNames_excludeId() {
    FolderEntity f1 = new FolderEntity();
    f1.setId("1");
    f1.setName("A");
    f1.setParentId("root");
    f1.setCreatedDate(LocalDateTime.now());

    FolderEntity f2 = new FolderEntity();
    f2.setId("2");
    f2.setName("B");
    f2.setParentId("root");
    f2.setCreatedDate(LocalDateTime.now());

    List<FolderEntity> folders = Arrays.asList(f1, f2);
    Set<String> names = NameIncrementHelper.collectSiblingNames(folders, "root", "1");
    assertFalse(names.contains("A"));
    assertTrue(names.contains("B"));
  }
}
