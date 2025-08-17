package com.ase.dms.services;

import com.ase.dms.dtos.FolderResponseDTO;
import com.ase.dms.entities.FolderEntity;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
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

  // Custom exception for folder not found
  public static class FolderNotFoundException extends RuntimeException {
    public FolderNotFoundException(String message) {
      super(message);
    }
  }

}
