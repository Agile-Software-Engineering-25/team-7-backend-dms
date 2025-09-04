package com.ase.dms.controllers;

import com.ase.dms.dtos.FolderResponseDTO;
import com.ase.dms.entities.FolderEntity;
import com.ase.dms.services.FolderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/dms/v1/folders")
public class FoldersController {

  private final FolderService folderService;
  public FoldersController(FolderService folderService) {
     this.folderService = folderService; }

  @GetMapping("/{id}")
  public ResponseEntity<FolderResponseDTO> getFolderContents(@PathVariable String id) {
    return ResponseEntity.ok(folderService.getFolderContents(id));
  }

  @PostMapping
  public ResponseEntity<FolderEntity> createFolder(@RequestBody FolderEntity folder) {
    return ResponseEntity.status(HttpStatus.CREATED).body(folderService.createFolder(folder));
  }

  @PatchMapping("/{id}")
  public ResponseEntity<FolderEntity> updateFolder(
      @PathVariable String id, @RequestBody FolderEntity folder) {
    return ResponseEntity.ok(folderService.updateFolder(id, folder));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteFolder(@PathVariable String id) {
    folderService.deleteFolder(id);
    return ResponseEntity.noContent().build();
  }
}
