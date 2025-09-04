package com.ase.dms.services;

import com.ase.dms.dtos.FolderResponseDTO;
import com.ase.dms.entities.FolderEntity;
import com.ase.dms.entities.DocumentEntity;
import com.ase.dms.helpers.NameIncrementHelper;
import com.ase.dms.repositories.FolderRepository;
import com.ase.dms.repositories.DocumentRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FolderServiceImpl implements FolderService {

  private final FolderRepository folders;
  private final DocumentRepository documents;

  public FolderServiceImpl(FolderRepository folders, DocumentRepository documents) {
    this.folders = folders;
    this.documents = documents;
  }

  @Override
  public FolderResponseDTO getFolderContents(String id) {
    FolderEntity folder = folders.findById(id)
      .orElseThrow(() -> new RuntimeException("Ordner nicht gefunden"));

    List<FolderEntity> subfolders = folders.findByParentId(id);
    List<DocumentEntity> docs = documents.findByFolderId(id);

    return new FolderResponseDTO(folder, subfolders, docs);
  }

  @Override @Transactional
  public FolderEntity createFolder(FolderEntity folder) {
    folder.setId(UUID.randomUUID().toString());
    folder.setCreatedDate(LocalDateTime.now());
    Set<String> siblingNames = NameIncrementHelper.collectSiblingNames(
        documents.findByFolderId(folder.getParentId()), folder.getParentId(), null);
    String uniqueName = NameIncrementHelper.getIncrementedName(folder.getName(), siblingNames);
    folder.setName(uniqueName);
    return folders.save(folder);
  }

  @Override @Transactional
  public FolderEntity updateFolder(String id, FolderEntity folder) {
    FolderEntity existing = folders.findById(id)
      .orElseThrow(() -> new RuntimeException("Ordner nicht gefunden"));
    Set<String> siblingNames = NameIncrementHelper.collectSiblingNames(
        //TODO Should we check the existing parent or incoming parent?
        documents.findByFolderId(existing.getParentId()), existing.getParentId(), null);
    String uniqueName = NameIncrementHelper.getIncrementedName(folder.getName(), siblingNames);
    existing.setName(uniqueName);
    // parentId/createdDate nach Bedarf updaten
    return folders.save(existing);
  }

  @Override @Transactional
  public void deleteFolder(String id) {
    if (!folders.existsById(id)){
       throw new RuntimeException("Ordner nicht gefunden");
    }
    // optional: erst Dokumente im Ordner löschen
    folders.deleteById(id);
  }
}
