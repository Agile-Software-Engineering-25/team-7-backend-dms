package com.ase.dms.repositories;

import com.ase.dms.entities.FolderEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FolderRepository extends JpaRepository<FolderEntity, String> {
  List<FolderEntity> findByParentId(String parentId);
  Optional<FolderEntity> findByNameAndParentIdIsNull(String name);
}
