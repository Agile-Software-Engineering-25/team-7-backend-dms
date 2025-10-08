package com.ase.dms.services;

import com.ase.dms.dtos.FolderResponseDTO;
import com.ase.dms.entities.FolderEntity;
import com.ase.dms.entities.DocumentEntity;
import com.ase.dms.exceptions.ErrorCodes;
import com.ase.dms.exceptions.FolderNotFoundException;
import com.ase.dms.exceptions.ValidationException;
import com.ase.dms.helpers.NameIncrementHelper;
import com.ase.dms.helpers.UuidValidator;
import com.ase.dms.repositories.FolderRepository;
import com.ase.dms.repositories.DocumentRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of FolderService for folder management operations.
 * Handles folder CRUD and navigation logic.
 */
@Service
public class FolderServiceImpl implements FolderService {

  /** Constant for the root folder identifier. */
  private static final String ROOT_ID = "root";

  private final FolderRepository folders;
  private final DocumentRepository documents;

  /**
   * Constructor for FolderServiceImpl.
   * @param folders the folder repository
   * @param documents the document repository
   */
  public FolderServiceImpl(final FolderRepository folders, final DocumentRepository documents) {
    this.folders = folders;
    this.documents = documents;
  }

  /**
   * Retrieves the contents of a folder by ID or 'root'.
   * @param id the folder UUID or 'root'
   * @return FolderResponseDTO with folder, subfolders, and documents
   */
  @Override
  public FolderResponseDTO getFolderContents(final String id) {
    FolderEntity folder;
    if (ROOT_ID.equals(id)) {
      folder = folders.findByNameAndParentIdIsNull(ROOT_ID)
        .orElseThrow(() -> new FolderNotFoundException("Root folder not found"));
    }
    else {
      UuidValidator.validateOrThrow(id);
      folder = folders.findById(id)
        .orElseThrow(() -> new FolderNotFoundException("Ordner "+ id + " nicht gefunden"));
    }

    List<FolderEntity> subfolders = folders.findByParentId(folder.getId());
    List<DocumentEntity> docs = documents.findByFolderId(folder.getId());

    return new FolderResponseDTO(folder, subfolders, docs);
  }

  /**
   * Creates a new folder.
   * @param folder the folder entity to create
   * @return the created FolderEntity
   */
  @Override
  @Transactional
  public FolderEntity createFolder(final FolderEntity folder) {
    UuidValidator.validateOrThrow(folder.getParentId());
    folders.findById(folder.getParentId())
        .orElseThrow(() -> new FolderNotFoundException(folder.getParentId()));
    folder.setId(UUID.randomUUID().toString());
    folder.setCreatedDate(LocalDateTime.now());

    // Get siblings for name conflict resolution
    List<FolderEntity> siblings = folders.findByParentId(folder.getParentId());
    Set<String> siblingNames = NameIncrementHelper.collectSiblingNames(
        siblings, folder.getParentId(), null);
    String uniqueName = NameIncrementHelper.getIncrementedName(folder.getName(), siblingNames);
    folder.setName(uniqueName);
    return folders.save(folder);
  }

  /**
   * Updates an existing folder.
   * @param id the folder UUID
   * @param incoming the folder entity with updates
   * @return the updated FolderEntity
   */
  @Override
  @Transactional
  public FolderEntity updateFolder(final String id, final FolderEntity incoming) {
    UuidValidator.validateOrThrow(id);
    FolderEntity existing = folders.findById(id)
        .orElseThrow(() -> new FolderNotFoundException(id));

    if (incoming.getParentId() != null && !incoming.getParentId().equals(existing.getParentId())) {
      UuidValidator.validateOrThrow(incoming.getParentId());
      folders.findById(incoming.getParentId())
          .orElseThrow(() -> new FolderNotFoundException(incoming.getParentId()));
      existing.setParentId(incoming.getParentId());
    }

    if (incoming.getName() != null) {
      // Bei Parent-Wechsel den neuen Parent für Namenskonflikt-Prüfung verwenden
      String targetParentId = incoming.getParentId() != null ? incoming.getParentId() : existing.getParentId();
      Set<String> siblingNames = NameIncrementHelper.collectSiblingNames(
          folders.findByParentId(targetParentId), targetParentId, existing.getId());
      existing.setName(NameIncrementHelper.getIncrementedName(incoming.getName(), siblingNames));
    }
    return folders.save(existing);
  }

  /**
   * Deletes a folder by ID with protection against deleting non-empty folders.
   * @param id the folder UUID
   * @throws ValidationException if folder contains subfolders or documents
   */
  @Override
  @Transactional
  public void deleteFolder(final String id) {
    UuidValidator.validateOrThrow(id);
    if (!folders.existsById(id)) {
      throw new FolderNotFoundException(id);
    }

    List<FolderEntity> subfolders = folders.findByParentId(id);
    if (!subfolders.isEmpty()) {
      throw new ValidationException(ErrorCodes.VAL_CHILDREN_FOLDER,
          "Cannot delete folder: contains " + subfolders.size() + " subfolder(s). Please delete or move all subfolders first.");
    }

    List<DocumentEntity> documents = this.documents.findByFolderId(id);
    if (!documents.isEmpty()) {
      throw new ValidationException(ErrorCodes.FOLDER_NOT_EMPTY,
          "Cannot delete folder: contains " + documents.size() + " document(s). Please delete or move all documents first.");
    }

    // Folder is empty, safe to delete
    folders.deleteById(id);
  }
}
