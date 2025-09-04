package com.ase.dms.components;

import com.ase.dms.entities.FolderEntity;
import com.ase.dms.repositories.FolderRepository;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import java.time.LocalDateTime;

@Component
public class DatabaseInitializer {
  private final FolderRepository folderRepository;

  @Autowired
  public DatabaseInitializer(FolderRepository folderRepository) {
    this.folderRepository = folderRepository;
  }

  @EventListener(ApplicationReadyEvent.class)
  public void createRootFolderIfNotExists() {
    boolean rootExists = folderRepository.findByNameAndParentIdIsNull("root").isPresent();
    if (!rootExists) {
      FolderEntity root = new FolderEntity();
      root.setId(java.util.UUID.randomUUID().toString());
      root.setName("root");
      root.setParentId(null);
      root.setCreatedDate(LocalDateTime.now());
      folderRepository.save(root);
    }
  }
}

