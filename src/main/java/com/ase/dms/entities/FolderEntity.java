package com.ase.dms.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Entity @NoArgsConstructor @AllArgsConstructor
public class FolderEntity {
  @Id
  @Schema(accessMode = Schema.AccessMode.READ_ONLY, description = "Eindeutige ID des Ordners", example = "03d3d491-1fa7-437f-b617-698a4d4c9d84")
  private String id;

  @NotBlank
  @Size(min = 1, max = 255)
  @Schema(description = "Name des Ordners", example = "Projekte")
  private String name;

  @Schema(description = "ID des übergeordneten Ordners als uuid", example = "c12b8e51-6c40-42b6-86e9-d8cf823f4d34")
  private String parentId;

  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
  @Schema(accessMode = Schema.AccessMode.READ_ONLY, description = "Erstellungsdatum")
  private LocalDateTime createdDate;
}
