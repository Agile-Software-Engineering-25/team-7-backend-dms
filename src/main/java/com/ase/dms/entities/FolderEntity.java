package com.ase.dms.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "folder_entity")
public class FolderEntity {
  @Id
  @Schema(accessMode = Schema.AccessMode.READ_ONLY,
      description = "Eindeutige ID des Ordners",
      example = "03d3d491-1fa7-437f-b617-698a4d4c9d84")
  private String id;

  @NotBlank
  @Size(min = 1, max = 255)
  @Schema(description = "Name des Ordners", example = "Projekte")
  private String name;

  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
  @Schema(accessMode = Schema.AccessMode.READ_ONLY, description = "Erstellungsdatum")
  private LocalDateTime createdDate;

  // JPA Relationships - this handles the foreign key
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "parentId")
  @JsonBackReference("parent-subfolders")
  @ToString.Exclude
  @Schema(description = "ID des übergeordneten Ordners als JPA Reference", example = "c12b8e51-6c40-42b6-86e9-d8cf823f4d34")
  private FolderEntity parent;

  @OneToMany(mappedBy = "parent", cascade = {CascadeType.REMOVE}, fetch = FetchType.LAZY)
  @JsonManagedReference("parent-subfolders")
  @ToString.Exclude
  @Schema(description = "Liste der Unterordner")
  private List<FolderEntity> subfolders = new ArrayList<>();

  @OneToMany(mappedBy = "folder", cascade = {CascadeType.REMOVE}, fetch = FetchType.LAZY)
  @JsonManagedReference("folder-documents")
  @ToString.Exclude
  @Schema(description = "Liste der Dokumente in diesem Ordner")
  private List<DocumentEntity> documents = new ArrayList<>();

  // Convenience method to get parent ID without loading the entity
  public String getParentId() {
    return parent != null ? parent.getId() : null;
  }

  // Convenience method to set parent by ID
  public void setParentId(String parentId) {
    if (parentId != null) {
      this.parent = new FolderEntity();
      this.parent.setId(parentId);
    } else {
      this.parent = null;
    }
  }
}
