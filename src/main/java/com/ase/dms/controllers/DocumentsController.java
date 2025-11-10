package com.ase.dms.controllers;

import com.ase.dms.entities.DocumentEntity;
import com.ase.dms.entities.TagEntity;
import com.ase.dms.services.DocumentService;
import com.ase.dms.services.MinIOService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/v1/documents")
@Tag(name = "Documents", description = "Document management operations")
public class DocumentsController {

  private final DocumentService documentService;
  private final MinIOService minIOService;

  public DocumentsController(DocumentService documentService, MinIOService minIOService) {
    this.documentService = documentService;
    this.minIOService = minIOService;
  }

  @Operation(summary = "Get document by ID")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Document found"),
      @ApiResponse(responseCode = "400", ref = "#/components/responses/BadRequestResponse"),
      @ApiResponse(responseCode = "404", ref = "#/components/responses/DocumentNotFoundResponse"),
      @ApiResponse(responseCode = "500", ref = "#/components/responses/InternalServerErrorResponse")
  })
  @GetMapping("/{id}")
  public ResponseEntity<DocumentEntity> getDocumentById(
      @Parameter(description = "Document UUID") @PathVariable String id) {
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
      @Parameter(description = "File to upload", required = true) @RequestParam("file") MultipartFile file,
      @Parameter(description = "Target folder UUID", required = true) @RequestParam("folderId") String folderId,
      @Parameter(description = "Document Tags") @RequestParam(value = "tags", required = false) String[] tags) {
    DocumentEntity doc = documentService.createDocument(file, folderId, tags);
    return ResponseEntity.status(HttpStatus.CREATED).body(doc);
  }

  @Operation(summary = "Update document metadata")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Document updated successfully"),
      @ApiResponse(responseCode = "400", ref = "#/components/responses/ValidationErrorResponse"),
      @ApiResponse(responseCode = "404", ref = "#/components/responses/DocumentNotFoundResponse")
  })
  @PatchMapping("/{id}")
  public ResponseEntity<DocumentEntity> updateDocument(
      @Parameter(description = "Document UUID") @PathVariable String id,
      @RequestBody DocumentEntity document) {
    return ResponseEntity.ok(documentService.updateDocument(id, document));
  }

  @Operation(summary = "Delete a document")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Document deleted successfully"),
      @ApiResponse(responseCode = "400", ref = "#/components/responses/BadRequestResponse"),
      @ApiResponse(responseCode = "404", ref = "#/components/responses/DocumentNotFoundResponse")
  })
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteDocument(
      @Parameter(description = "Document UUID") @PathVariable String id) {
    documentService.deleteDocument(id);
    return ResponseEntity.noContent().build();
  }

  @Operation(summary = "Download document file")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "File downloaded successfully",
          content = @Content(mediaType = "application/octet-stream",
              schema = @Schema(type = "string", format = "binary"))),
      @ApiResponse(responseCode = "404", ref = "#/components/responses/DocumentNotFoundResponse")
  })
  @GetMapping("/{id}/download")
  public ResponseEntity<byte[]> downloadDocument(
      @Parameter(description = "Document UUID") @PathVariable String id) {
    DocumentEntity document = documentService.getDocument(id);
    byte[] data = minIOService.getObjectData(id);

    HttpHeaders headers = new HttpHeaders();
    if (document.getType() != null && !document.getType().isEmpty()) {
      headers.setContentType(MediaType.parseMediaType(document.getType()));
    }
    else {
      headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
    }

    String filename = document.getName() != null ? document.getName() : "document";
    headers.setContentDispositionFormData("attachment", filename);
    headers.setContentLength(data.length);

    return new ResponseEntity<>(data, headers, HttpStatus.OK);
  }

  @Operation(summary = "Get document tags")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Document tags found"),
      @ApiResponse(responseCode = "404", ref = "#/components/responses/DocumentNotFoundResponse")
  })
  @GetMapping("/{id}/tags")
  public ResponseEntity<TagEntity[]> getDocumentTags(
      @Parameter(description = "Document UUID") @PathVariable String id
  ) {
    return ResponseEntity.ok(documentService.getDocument(id).getTags().toArray(new TagEntity[0]));
  }

  @Operation(summary = "Update document tags")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Document tags updated successfully"),
      @ApiResponse(responseCode = "400", ref = "#/components/responses/ValidationErrorResponse"),
      @ApiResponse(responseCode = "404", ref = "#/components/responses/DocumentNotFoundResponse")
  })
  @PutMapping("/{id}/tags")
  public ResponseEntity<DocumentEntity> updateDocumentTags(
      @Parameter(description = "Document UUID") @PathVariable String id,
      @RequestBody String[] tags
  ) {
    return ResponseEntity.accepted().body(documentService.setDocumentTags(id, tags));
  }

  @Operation(summary = "Convert document file to pdf")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "File converted successfully",
          content = @Content(mediaType = "application/octet-stream",
              schema = @Schema(type = "string", format = "binary"))),
      @ApiResponse(responseCode = "404", ref = "#/components/responses/DocumentNotFoundResponse"),
      @ApiResponse(responseCode = "415", description = "Unsupported Media Type - cannot convert the provided document")
  })
  @GetMapping("/{id}/pdfconverter")
  public ResponseEntity<byte[]> convertDocument(
      @Parameter(description = "Document UUID") @PathVariable String id) {
    DocumentEntity document = documentService.getDocument(id);
    byte[] pdfData = documentService.convertDocument(document);

    String name = document.getName() != null ? document.getName() : "document";
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_PDF);
    headers.setContentDispositionFormData("attachment", name.endsWith(".pdf") ? name : name + ".pdf");
    headers.setContentLength(pdfData.length);
    return new ResponseEntity<>(pdfData, headers, HttpStatus.OK);
  }
}
