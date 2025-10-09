package com.ase.dms.repositories;

import com.ase.dms.entities.FolderEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FolderRepository extends JpaRepository<FolderEntity, String> {
  // Find root folders (folders with no parent)
  Optional<FolderEntity> findByNameAndParentIsNull(String name);

  // Find subfolders by parent
  List<FolderEntity> findByParent(FolderEntity parent);

  // Find subfolders by parent ID using custom query
  @Query("SELECT f FROM FolderEntity f WHERE f.parent.id = :parentId")
  List<FolderEntity> findByParentId(@Param("parentId") String parentId);

  // Find all root folders
  List<FolderEntity> findByParentIsNull();
}
