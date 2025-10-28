package com.ase.dms.controllers;

import com.ase.dms.entities.DocumentEntity;
import com.ase.dms.services.DocumentService;

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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import org.jodconverter.core.DocumentConverter;
import org.jodconverter.core.office.OfficeException;
import org.jodconverter.core.document.DocumentFormat;
import org.jodconverter.core.document.DocumentFormatRegistry;
import org.jodconverter.core.document.DefaultDocumentFormatRegistry;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.Objects;

@RestController
@RequestMapping("/v1/documents")
@Tag(name = "Documents", description = "Document management operations")
public class DocumentsController {

  private final DocumentService documentService;
  private final DocumentConverter documentConverter;

  public DocumentsController(DocumentService documentService, DocumentConverter documentConverter) {
     this.documentService = documentService;
     this.documentConverter = documentConverter;
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
      @Parameter(description = "Target folder UUID", required = true) @RequestParam("folderId") String folderId) {
    DocumentEntity doc = documentService.createDocument(file, folderId);
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

    if (document.getData() == null || document.getData().length == 0) {
      return ResponseEntity.notFound().build();
    }

    HttpHeaders headers = new HttpHeaders();
    if (document.getType() != null && !document.getType().isEmpty()) {
      headers.setContentType(MediaType.parseMediaType(document.getType()));
    }
    else {
      headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
    }

    String filename = document.getName() != null ? document.getName() : "document";
    headers.setContentDispositionFormData("attachment", filename);
    headers.setContentLength(document.getData().length);

    return new ResponseEntity<>(document.getData(), headers, HttpStatus.OK);
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

    if (document.getData() == null || document.getData().length == 0) {
      return ResponseEntity.notFound().build();
    }

    // Determine if the document is an office document we can convert
    String type = document.getType() != null ? document.getType().toLowerCase(Locale.ROOT) : "";
    String name = document.getName() != null ? document.getName() : "document";

    // If already a PDF, just return it
    if (type.contains("pdf") || name.toLowerCase(Locale.ROOT).endsWith(".pdf")) {
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_PDF);
      headers.setContentDispositionFormData("attachment", name.endsWith(".pdf") ? name : name + ".pdf");
      headers.setContentLength(document.getData().length);
      return new ResponseEntity<>(document.getData(), headers, HttpStatus.OK);
    }

    // Supported input types/extensions (common office types)
    boolean supported = type.contains("msword") || type.contains("officedocument") ||
        type.contains("vnd.openxmlformats-officedocument") || name.toLowerCase(Locale.ROOT).matches(".*\\.(doc|docx|xls|xlsx|ppt|pptx)$");

    if (!supported) {
      // Not supported for conversion; return 415 Unsupported Media Type
      return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).build();
    }

    try (ByteArrayInputStream in = new ByteArrayInputStream(document.getData());
         ByteArrayOutputStream out = new ByteArrayOutputStream()) {

      // Determine source and target document formats using the registry
      DocumentFormatRegistry registry = DefaultDocumentFormatRegistry.getInstance();

      // Try to resolve source format from MIME type first, then from file extension
      DocumentFormat sourceFormat = null;
      if (document.getType() != null && !document.getType().isEmpty()) {
        sourceFormat = registry.getFormatByMediaType(document.getType());
      }
      if (sourceFormat == null) {
        String ext = "";
        int dot = name.lastIndexOf('.');
        if (dot >= 0 && dot < name.length() - 1) {
          ext = name.substring(dot + 1).toLowerCase(Locale.ROOT);
        }
        if (!ext.isEmpty()) {
          sourceFormat = registry.getFormatByExtension(ext);
        }
      }

      if (sourceFormat == null) {
        // Could not determine input format -> unsupported
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).build();
      }

      DocumentFormat pdfFormat = registry.getFormatByMediaType("application/pdf");
      if (pdfFormat == null) {
        pdfFormat = registry.getFormatByExtension("pdf");
      }

      if (pdfFormat == null) {
        // Should not happen in normal environments; return 500 to indicate server misconfiguration
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
      }

      // Ensure pdfFormat is non-null for static analysis
      Objects.requireNonNull(pdfFormat, "PDF DocumentFormat not available");

      // Convert to PDF using jodconverter with resolved formats
      documentConverter.convert(in).as(sourceFormat).to(out).as(pdfFormat).execute();

      byte[] pdfBytes = out.toByteArray();

      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_PDF);
      String pdfName = name.toLowerCase(Locale.ROOT).endsWith(".pdf") ? name : name + ".pdf";
      headers.setContentDispositionFormData("attachment", pdfName);
      headers.setContentLength(pdfBytes.length);

      return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);

    } catch (OfficeException | IOException e) {
      // Conversion failed
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }
}
