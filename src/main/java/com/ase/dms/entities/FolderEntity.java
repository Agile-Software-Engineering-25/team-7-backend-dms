package com.ase.dms.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Entity @NoArgsConstructor @AllArgsConstructor
public class FolderEntity {
  @Id
  private String id;          // z.B. UUID
  private String name;
  private String parentId;

  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
  private LocalDateTime createdDate;
}
