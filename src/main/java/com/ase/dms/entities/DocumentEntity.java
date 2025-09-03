package com.ase.dms.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import java.time.LocalDateTime;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Entity @NoArgsConstructor @AllArgsConstructor @Schema(
    name = "Document",
    description = "Repräsentiert ein Dokument im DMS",
    example = """
        {
            "id": "123e4567-e89b-12d3-a456-426614174000",
            "name": "vertrag",
            "type": "pdf",
            "size": 1024000
        }
        """
)
public class DocumentEntity {
  @Id
  @Schema(accessMode = Schema.AccessMode.READ_ONLY, description = "Eindeutige ID des Dokuments")
  private String id;

  @NotBlank
  @Size(min = 1, max = 255)
  @Schema(description = "Name des Dokuments", example = "wichtiges_dokument")
  private String name;

  @NotBlank
  @Schema(description = "Dateityp", example = "pdf")
  private String type;

  @Schema(accessMode = Schema.AccessMode.READ_ONLY, description = "Dateigröße in Bytes")
  private long size;

  @Schema(description = "ID des übergeordneten Ordners als uuid", example = "123e4567-e89b-12d3-a456-426614174000")
  private String folderId;

  @Schema(description = "ID des Besitzers (User) des Dokuments")
  private String ownerId;

  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
  @Schema(accessMode = Schema.AccessMode.READ_ONLY, description = "Erstellungsdatum")
  private LocalDateTime createdDate;

  @Schema(accessMode = Schema.AccessMode.READ_ONLY, description = "Download-URL des Dokuments")
  private String downloadUrl;

  @Lob
  @Schema(accessMode = Schema.AccessMode.READ_ONLY, description = "Dateiinhalt")
  private byte[] data;
}
