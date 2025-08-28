package com.ase.dms.dtos;

import com.ase.dms.entities.DocumentEntity;
import com.ase.dms.entities.FolderEntity;
import java.util.List;
import lombok.Data;
import lombok.AllArgsConstructor;

@Data @AllArgsConstructor
public class FolderResponseDTO {
  private FolderEntity folder;
  private List<FolderEntity> subfolders;
  private List<DocumentEntity> documents;
}
