package com.ase.dms.services;

import com.ase.dms.entities.FolderEntity;

public interface FolderService {
  FolderEntity getFolderContents(String id);
  FolderEntity createFolder(FolderEntity folder);
  FolderEntity updateFolder(String id, FolderEntity folder);
  void deleteFolder(String id);
}
