package com.ase.dms.controllers;

import com.ase.dms.entities.DocumentEntity;
import com.ase.dms.services.DocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/dms/v1/documents")
public class DocumentsController {

  private final DocumentService documentService;
  public DocumentsController(DocumentService documentService) {
     this.documentService = documentService; }

  @GetMapping("/{id}")
  public ResponseEntity<DocumentEntity> getDocumentById(
      @Parameter(description = "Document UUID") @PathVariable String id) {
    return ResponseEntity.ok(documentService.getDocument(id));
  }

  // <-- NEU: kein {id} im Pfad, folderId als RequestParam
  @PostMapping
  public ResponseEntity<DocumentEntity> uploadDocument(
      @Parameter(description = "File to upload", required = true) @RequestParam("file") MultipartFile file,
      @Parameter(description = "Target folder UUID", required = true) @RequestParam("folderId") String folderId) {
    DocumentEntity doc = documentService.createDocument(file, folderId);
    return ResponseEntity.status(HttpStatus.CREATED).body(doc);
  }

  @PatchMapping("/{id}")
  public ResponseEntity<DocumentEntity> updateDocument(
      @Parameter(description = "Document UUID") @PathVariable String id,
      @RequestBody DocumentEntity document) {
    return ResponseEntity.ok(documentService.updateDocument(id, document));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteDocument(
      @Parameter(description = "Document UUID") @PathVariable String id) {
    documentService.deleteDocument(id);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/{id}/download")
  public ResponseEntity<byte[]> downloadDocument(
      @Parameter(description = "Document UUID") @PathVariable String id) {
    DocumentEntity document = documentService.getDocument(id);

    if (document.getData() == null || document.getData().length == 0) {
      return ResponseEntity.notFound().build();
    }

    HttpHeaders headers = new HttpHeaders();
    if (document.getType() != null && !document.getType().isEmpty()) {
      headers.setContentType(MediaType.parseMediaType(document.getType()));
    } else {
      headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
    }

    String filename = document.getName() != null ? document.getName() : "document";
    headers.setContentDispositionFormData("attachment", filename);
    headers.setContentLength(document.getData().length);

    return new ResponseEntity<>(document.getData(), headers, HttpStatus.OK);
  }
}
