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

@Data @Entity @NoArgsConstructor @AllArgsConstructor
public class DocumentEntity {
  @Id
  @Schema(accessMode = Schema.AccessMode.READ_ONLY,
      description = "Eindeutige ID des Dokuments",
      example = "4111b676-474c-4014-a7ee-53fc5cb90127")
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

  @Schema(description = "ID des übergeordneten Ordners als uuid", example = "ef9b2274-817e-4cba-879e-383548577f4e")
  private String folderId;

  @Schema(description = "ID des Besitzers (User) des Dokuments")
  private String ownerId;

  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
  @Schema(accessMode = Schema.AccessMode.READ_ONLY, description = "Erstellungsdatum")
  private LocalDateTime createdDate;

  @Schema(accessMode = Schema.AccessMode.READ_ONLY,
      description = "Download-URL des Dokuments",
      example = "/dms/v1/documents/4111b676-474c-4014-a7ee-53fc5cb90127/download")
  private String downloadUrl;

  @Lob
  @Schema(accessMode = Schema.AccessMode.READ_ONLY, description = "Dateiinhalt")
  private byte[] data;
}
