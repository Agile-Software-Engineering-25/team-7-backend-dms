package com.ase.dms.repositories;

import com.ase.dms.entities.TagEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TagRepository extends JpaRepository<TagEntity, String> {
  boolean existsByName(String name);
}
