package com.ase.dms.repositories;

import com.ase.dms.entities.FolderEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FolderRepository extends JpaRepository<FolderEntity, String> {
  List<FolderEntity> findByParentId(String parentId);
}
