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
  public DocumentsController(DocumentService documentService) {
     this.documentService = documentService; }

  @GetMapping("/{id}")
  public ResponseEntity<DocumentEntity> getDocumentById(@PathVariable String id) {
    return ResponseEntity.ok(documentService.getDocument(id));
  }

  @Operation(summary = "Upload a new document")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "201", description = "Document uploaded successfully"),
    @ApiResponse(responseCode = "400", ref = "#/components/responses/DocumentUploadFailedResponse"),
    @ApiResponse(responseCode = "413", ref = "#/components/responses/PayloadTooLargeResponse")
  })
  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<DocumentEntity> uploadDocument(
      @RequestParam("file") MultipartFile file,
      @RequestParam("folderId") String folderId) {
    DocumentEntity doc = documentService.createDocument(file, folderId);
    return ResponseEntity.status(HttpStatus.CREATED).body(doc);
  }

  @PatchMapping("/{id}")
  public ResponseEntity<DocumentEntity> updateDocument(
      @PathVariable String id, @RequestBody DocumentEntity document) {
    return ResponseEntity.ok(documentService.updateDocument(id, document));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteDocument(@PathVariable String id) {
    documentService.deleteDocument(id);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/{id}/download")
  public ResponseEntity<?> downloadDocument(@PathVariable String id) {
    // Minimal: Datei bytes zurückgeben; erstmal nur Platzhalter-Text
    return ResponseEntity.ok("Download kommt später: " + id);
  }
}
