package com.ase.dms.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "document_entity")
public class DocumentEntity {
  @Id
  @EqualsAndHashCode.Include
  @Schema(accessMode = Schema.AccessMode.READ_ONLY, description = "Eindeutige ID des Dokuments", example = "4111b676-474c-4014-a7ee-53fc5cb90127")
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

  @Schema(description = "ID des Besitzers (User) des Dokuments", example = "9ad5c7cf-273c-4243-91a3-f7969f6dc985")
  private String ownerId;

  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
  @Schema(accessMode = Schema.AccessMode.READ_ONLY, description = "Erstellungsdatum")
  private LocalDateTime createdDate;

  @Schema(accessMode = Schema.AccessMode.READ_ONLY,
      description = "Download-URL des Dokuments",
      example = "https://sau-portal.de/dms/v1/documents/4111b676-474c-4014-a7ee-53fc5cb90127/download")
  private String downloadUrl;

  // TODO: Getter and Setter for binaries to use MINIO Service
  @Lob
  @Schema(accessMode = Schema.AccessMode.READ_ONLY, description = "Dateiinhalt")
  private byte[] data;

  // JPA Relationship
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "folderId")
  @JsonBackReference("folder-documents")
  @ToString.Exclude
  @Schema(hidden = true, description = "ID des übergeordneten Ordners als JPA Reference", example = "ef9b2274-817e-4cba-879e-383548577f4e")
  private FolderEntity folder;

  // Convenience method to get folder ID without loading the entity
  public String getFolderId() {
    return folder != null ? folder.getId() : null;
  }

  @Schema(description = "ID of the parent folder", example = "ef9b2274-817e-4cba-879e-383548577f4e")
  public void setFolderId(String folderId) {
    if (folderId != null) {
      this.folder = new FolderEntity();
      this.folder.setId(folderId);
    } else {
      this.folder = null;
    }
  }

}
