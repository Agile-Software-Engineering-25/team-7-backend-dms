package com.ase.dms.repositories;

import com.ase.dms.entities.DocumentEntity;
import com.ase.dms.entities.FolderEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DocumentRepository extends JpaRepository<DocumentEntity, String> {
  // Find documents by folder
  List<DocumentEntity> findByFolder(FolderEntity folder);

  // Find documents by folder ID using custom query
  @Query("SELECT d FROM DocumentEntity d WHERE d.folder.id = :folderId")
  List<DocumentEntity> findByFolderId(@Param("folderId") String folderId);
}
