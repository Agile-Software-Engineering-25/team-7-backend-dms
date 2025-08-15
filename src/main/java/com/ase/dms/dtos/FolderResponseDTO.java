package com.ase.dms.dtos;

import com.ase.dms.entities.DocumentEntity;
import com.ase.dms.entities.FolderEntity;
import java.util.List;
import lombok.Data;

@Data
public class FolderResponseDTO {
  private final FolderEntity folder;
  private final List<FolderEntity> subfolders;
  private final List<DocumentEntity> documents;
}
