package com.ase.dms.services;

import com.ase.dms.entities.FolderEntity;
import com.ase.dms.exceptions.FolderNotFoundException;
import com.ase.dms.helpers.NameIncrementHelper;
import com.ase.dms.helpers.UuidValidator;
import com.ase.dms.repositories.FolderRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
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

 @Autowired
  private final MinIOService minIOService;

  /**
   * Constructor for FolderServiceImpl.
   *
   * @param folders the folder repository
   */
  public FolderServiceImpl(final FolderRepository folders,
   final MinIOService minIOService
  ) {
    this.folders = folders;
    this.minIOService = minIOService;
  }

  /**
   * Retrieves the contents of a folder by ID or 'root'.
   *
   * @param id the folder UUID or 'root'
   * @return FolderEntity with subfolders and documents accessible via JPA
   *         relationships
   */
  @Override
  public FolderEntity getFolderContents(final String id) {
    FolderEntity folder;
    if (ROOT_ID.equals(id)) {
      folder = folders.findByNameAndParentIsNull(ROOT_ID)
          .orElseThrow(() -> new FolderNotFoundException("Root folder not found"));
    }
    else {
      UuidValidator.validateOrThrow(id);
      folder = folders.findById(id)
          .orElseThrow(() -> new FolderNotFoundException("Ordner " + id + " nicht gefunden"));
    }

    // JPA relationships automatically provides access to subfolders and documents
    return folder;
  }

  /**
   * Creates a new folder.
   *
   * @param folder the folder entity to create
   * @return the created FolderEntity
   */
  @Override
  @Transactional
  public FolderEntity createFolder(final FolderEntity folder) {
    UuidValidator.validateOrThrow(folder.getParentId());
    // Load the parent folder and set the relationship directly
    FolderEntity parent = folders.findById(folder.getParentId())
        .orElseThrow(() -> new FolderNotFoundException(folder.getParentId()));

    folder.setId(UUID.randomUUID().toString());
    folder.setCreatedDate(LocalDateTime.now());

    // Set the parent relationship directly - cleaner approach
    folder.setParent(parent);

    // Get siblings for name conflict resolution using JPA relationship
    List<FolderEntity> siblings = parent.getSubfolders();
    Set<String> siblingNames = NameIncrementHelper.collectSiblingNames(
        siblings, folder.getParentId(), null);
    String uniqueName = NameIncrementHelper.getIncrementedName(folder.getName(), siblingNames);
    folder.setName(uniqueName);
    return folders.save(folder);
  }

  /**
   * Updates an existing folder.
   *
   * @param id       the folder UUID
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
      FolderEntity newParent = folders.findById(incoming.getParentId())
          .orElseThrow(() -> new FolderNotFoundException(incoming.getParentId()));
      // Set the parent relationship directly
      existing.setParent(newParent);
    }

    if (incoming.getName() != null) {
      // Bei Parent-Wechsel den neuen Parent für Namenskonflikt-Prüfung verwenden
      String targetParentId = incoming.getParentId() != null ? incoming.getParentId() : existing.getParentId();
      FolderEntity targetParent = folders.findById(targetParentId)
          .orElseThrow(() -> new FolderNotFoundException(targetParentId));

      Set<String> siblingNames = NameIncrementHelper.collectSiblingNames(
          targetParent.getSubfolders(), targetParentId, existing.getId());
      existing.setName(NameIncrementHelper.getIncrementedName(incoming.getName(), siblingNames));
    }
    return folders.save(existing);
  }

  /**
   * Deletes a folder by ID with JPA cascade handling.
   * JPA will automatically delete all subfolders and documents due to cascade
   * configuration.
   *
   * @param id the folder UUID
   */
  @Override
  @Transactional
  public void deleteFolder(final String id) {
    UuidValidator.validateOrThrow(id);

    // To delete all the data from minio,
    // we would need to manually traverse
    // and delete documents first.
    FolderEntity folder = getFolderContents(id);

    deleteDocumentsRecursively(folder);

    // With JPA cascade operations, deleting the folder will automatically
    // delete all subfolders and documents - no warnings
    folders.deleteById(id);
  }

  private void deleteDocumentsRecursively(FolderEntity folder) {
    folder.getSubfolders().forEach(this::deleteDocumentsRecursively);
    folder.getDocuments().forEach(doc -> minIOService.deleteObject(doc.getId()));
  }
}
