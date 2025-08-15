package com.ase.dms.controllers;

import com.ase.dms.entities.DocumentEntity;
import com.ase.dms.services.DocumentService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/dms/v1/documents")
public class DocumentsController {

  private final DocumentService documentService;

  @Autowired
  public DocumentsController(DocumentService documentService) {
    this.documentService = documentService;
  }

  @GetMapping("/{id}")
  public ResponseEntity<DocumentEntity> getDocumentById(@PathVariable String id) {
    return ResponseEntity.ok(documentService.getDocument(id));
  }

  @PostMapping
  public ResponseEntity<DocumentEntity> uploadDocument(
      @RequestParam("file") MultipartFile file,
      @RequestParam("folderId") String folderId) {
    DocumentEntity doc = documentService.createDocument(file, folderId);
    return ResponseEntity.status(HttpStatus.CREATED).body(doc);
  }

  @PatchMapping("/{id}")
  public ResponseEntity<DocumentEntity> updateDocument(
      @PathVariable String id,
      @RequestBody DocumentEntity document) {
    return ResponseEntity.ok(documentService.updateDocument(id, document));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteDocument(@PathVariable String id) {
    documentService.deleteDocument(id);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/{id}/download")
  public ResponseEntity<String> downloadDocument(@PathVariable String id) {
    return ResponseEntity.ok().body("Download functionality is not implemented yet for document: " + id);
  }
}
