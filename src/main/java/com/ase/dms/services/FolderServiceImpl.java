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
import com.ase.dms.exceptions.FolderNotFoundException;

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
    if (!id.equals("root")) {
      // Validate UUID
      try {
        UUID.fromString(id);
      } catch (Exception e) {
        throw new FolderNotFoundException("Invalid folder id: must be UUID or 'root'");
      }
    }

    FolderEntity folder;
    if (id.equals("root")) {
      folder = folders.findByNameAndParentIdIsNull("root")
        .orElseThrow(() -> new FolderNotFoundException("Root folder not found"));
    } else {
      folder = folders.findById(id)
        .orElseThrow(() -> new FolderNotFoundException("Ordner nicht gefunden"));
    }

    List<FolderEntity> subfolders = folders.findByParentId(folder.getId());
    List<DocumentEntity> docs = documents.findByFolderId(folder.getId());

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
  public FolderEntity updateFolder(String id, FolderEntity incoming) {
    FolderEntity existing = folders.findById(id)
        .orElseThrow(() -> new RuntimeException("Ordner nicht gefunden"));

    if (incoming.getName() != null) {
      // Bei Parent-Wechsel den neuen Parent für Namenskonflikt-Prüfung verwenden
      String targetParentId = incoming.getParentId() != null ? incoming.getParentId() : existing.getParentId();
      Set<String> siblingNames = NameIncrementHelper.collectSiblingNames(
          folders.findByParentId(targetParentId), targetParentId, existing.getId());
      existing.setName(NameIncrementHelper.getIncrementedName(incoming.getName(), siblingNames));
    }

    if (incoming.getParentId() != null) {
      existing.setParentId(incoming.getParentId());
    }

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
