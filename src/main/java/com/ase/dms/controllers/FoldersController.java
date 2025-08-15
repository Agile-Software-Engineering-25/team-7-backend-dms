package com.ase.dms.controllers;

import com.ase.dms.dtos.FolderResponseDTO;
import com.ase.dms.entities.FolderEntity;
import com.ase.dms.services.FolderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dms/v1/folders")
public class FoldersController {

  private final FolderService folderService;

  @Autowired
  public FoldersController(FolderService folderService) {
    this.folderService = folderService;
  }

  @GetMapping("/{id}")
  public ResponseEntity<FolderResponseDTO> getFolderContents(
      @PathVariable String id) {
    return ResponseEntity.ok(folderService.getFolderContents(id));
  }

  @PostMapping
  public ResponseEntity<FolderEntity> createFolder(
      @RequestBody FolderEntity folder) {
    FolderEntity newFolder = folderService.createFolder(folder);
    return ResponseEntity.status(HttpStatus.CREATED).body(newFolder);
  }

  @PatchMapping("/{id}")
  public ResponseEntity<FolderEntity> updateFolder(
      @PathVariable String id,
      @RequestBody FolderEntity folder) {
    return ResponseEntity.ok(folderService.updateFolder(id, folder));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteFolder(@PathVariable String id) {
    folderService.deleteFolder(id);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/{id}/download")
  public ResponseEntity<String> downloadFolder(@PathVariable String id) {
    return ResponseEntity.ok().body(
        "Download functionality is not implemented yet for folder: " + id);
  }
}
