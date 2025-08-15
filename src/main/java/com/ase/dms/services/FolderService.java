package com.ase.dms.services;

import com.ase.dms.dtos.FolderResponseDTO;
import com.ase.dms.entities.FolderEntity;
import org.springframework.stereotype.Service;

@Service
public interface FolderService {
  FolderResponseDTO getFolderContents(String id);
  FolderEntity createFolder(FolderEntity folder);
  FolderEntity updateFolder(String id, FolderEntity folder);
  void deleteFolder(String id);
  //Resource createFolderZip(String id);
}
