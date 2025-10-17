package com.ase.dms.controllers;

import com.ase.dms.entities.FolderEntity;
import com.ase.dms.services.FolderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/folders")
@Tag(name = "Folders", description = "Folder management and navigation operations")
public class FoldersController {

  private final FolderService folderService;

  public FoldersController(FolderService folderService) {
     this.folderService = folderService;
  }

  @Operation(summary = "Get folder contents",
             description = "Retrieves the contents of a folder. Use 'root' for the root folder.")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Folder contents retrieved successfully"),
    @ApiResponse(responseCode = "400", ref = "#/components/responses/BadRequestResponse"),
    @ApiResponse(responseCode = "404", ref = "#/components/responses/FolderNotFoundResponse")
  })
  @GetMapping("/{id}")
  public ResponseEntity<FolderEntity> getFolderContents(
      @Parameter(description = "Folder UUID or 'root'") @PathVariable String id) {
    return ResponseEntity.ok(folderService.getFolderContents(id));
  }

  @Operation(summary = "Create a new folder")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "201", description = "Folder created successfully"),
    @ApiResponse(responseCode = "400", ref = "#/components/responses/ValidationErrorResponse")
  })
  @PostMapping
  public ResponseEntity<FolderEntity> createFolder(@RequestBody FolderEntity folder) {
    return ResponseEntity.status(HttpStatus.CREATED).body(folderService.createFolder(folder));
  }

  @Operation(summary = "Update folder metadata")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Folder updated successfully"),
    @ApiResponse(responseCode = "400", ref = "#/components/responses/BadRequestResponse"),
    @ApiResponse(responseCode = "404", ref = "#/components/responses/FolderNotFoundResponse")
  })
  @PatchMapping("/{id}")
  public ResponseEntity<FolderEntity> updateFolder(
      @Parameter(description = "Folder UUID") @PathVariable String id,
      @RequestBody FolderEntity folder) {
    return ResponseEntity.ok(folderService.updateFolder(id, folder));
  }

  @Operation(summary = "Delete a folder")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "204", description = "Folder deleted successfully"),
    @ApiResponse(responseCode = "400", ref = "#/components/responses/BadRequestResponse"),
    @ApiResponse(responseCode = "404", ref = "#/components/responses/FolderNotFoundResponse")
  })
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteFolder(
      @Parameter(description = "Folder UUID") @PathVariable String id) {
    folderService.deleteFolder(id);
    return ResponseEntity.noContent().build();
  }
}
