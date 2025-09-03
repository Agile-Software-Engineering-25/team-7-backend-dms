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
  @Schema(accessMode = Schema.AccessMode.READ_ONLY, description = "Eindeutige ID des Ordners")
  private String id;

  @NotBlank
  @Size(min = 1, max = 255)
  @Schema(description = "Name des Ordners", example = "Projekte")
  private String name;

  @Schema(description = "ID des übergeordneten Ordners als uuid", example = "123e4567-e89b-12d3-a456-426614174000")
  private String parentId;

  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
  @Schema(accessMode = Schema.AccessMode.READ_ONLY, description = "Erstellungsdatum")
  private LocalDateTime createdDate;
}
