package com.ase.dms.repositories;

import com.ase.dms.entities.DocumentEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentRepository extends JpaRepository<DocumentEntity, String> {
  List<DocumentEntity> findByFolderId(String folderId);
}
