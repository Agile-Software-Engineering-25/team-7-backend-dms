package com.ase.dms.services;

import com.ase.dms.dtos.FolderResponseDTO;
import com.ase.dms.entities.FolderEntity;
import com.ase.dms.exceptions.FolderNotFoundException;
import com.ase.dms.helpers.NameIncrementHelper;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class FolderServiceImpl implements FolderService {

  private final Map<String, FolderEntity> folderStorage = new HashMap<>();

  public FolderServiceImpl() { // Initializing with a dummy folder for testing purposes
    String id = "test-id"; //UUID.randomUUID().toString();
    FolderEntity dummyFolder = new FolderEntity(id, "Dummy Folder", "root", LocalDateTime.now());
    folderStorage.put(id, dummyFolder);
  }

  @Override
  public FolderResponseDTO getFolderContents(String id) {
    if (!folderStorage.containsKey(id)) {
      throw new FolderNotFoundException("Ordner nicht gefunden");
    }

    FolderEntity folder = folderStorage.get(id);
    ArrayList<FolderEntity> subfolders = new ArrayList<>();
    for (FolderEntity subfolder : folderStorage.values()) {
      if (Objects.equals(subfolder.getParentId(), id)) {
        subfolders.add(subfolder);
      }
    }

    return new FolderResponseDTO(
        folder,
        subfolders,
        new ArrayList<>()
    );
  }

  @Override
  public FolderEntity createFolder(FolderEntity folder) {
    Set<String> siblingNames = NameIncrementHelper.collectSiblingNames(
        folderStorage.values(), folder.getParentId(), null);
    String uniqueName = NameIncrementHelper.getIncrementedName(folder.getName(), siblingNames);
    folder.setName(uniqueName);
    String id = UUID.randomUUID().toString();
    folder.setId(id);
    folderStorage.put(id, folder);
    return folder;
  }

  @Override
  public FolderEntity updateFolder(String id, FolderEntity folder) {
    if (!folderStorage.containsKey(id)) {
      throw new FolderNotFoundException("Ordner nicht gefunden");
    }
    Set<String> siblingNames = NameIncrementHelper.collectSiblingNames(
        folderStorage.values(), folder.getParentId(), id);
    String uniqueName = NameIncrementHelper.getIncrementedName(folder.getName(), siblingNames);
    folder.setName(uniqueName);
    folder.setId(id);
    folderStorage.put(id, folder);
    return folder;
  }

  @Override
  public void deleteFolder(String id) {
    if (!folderStorage.containsKey(id)) {
      throw new FolderNotFoundException("Ordner nicht gefunden");
    }
    folderStorage.remove(id);
  }
}
