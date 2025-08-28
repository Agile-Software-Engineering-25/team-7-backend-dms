package com.ase.dms.controllers;

import com.ase.dms.entities.DocumentEntity;
import com.ase.dms.services.DocumentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/dms/v1/documents")
public class DocumentsController {

  private final DocumentService documentService;
  public DocumentsController(DocumentService documentService) { this.documentService = documentService; }

  @GetMapping("/{id}")
  public ResponseEntity<?> getDocumentById(@PathVariable String id) {
    return ResponseEntity.ok(documentService.getDocument(id));
  }

  // <-- NEU: kein {id} im Pfad, folderId als RequestParam
  @PostMapping
  public ResponseEntity<?> uploadDocument(
      @RequestParam("file") MultipartFile file,
      @RequestParam("folderId") String folderId) {
    DocumentEntity doc = documentService.createDocument(file, folderId);
    return ResponseEntity.status(HttpStatus.CREATED).body(doc);
  }

  @PatchMapping("/{id}")
  public ResponseEntity<?> updateDocument(
      @PathVariable String id, @RequestBody DocumentEntity document) {
    return ResponseEntity.ok(documentService.updateDocument(id, document));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<?> deleteDocument(@PathVariable String id) {
    documentService.deleteDocument(id);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/{id}/download")
  public ResponseEntity<?> downloadDocument(@PathVariable String id) {
    // Minimal: Datei bytes zurückgeben; erstmal nur Platzhalter-Text
    return ResponseEntity.ok("Download kommt später: " + id);
  }
}
