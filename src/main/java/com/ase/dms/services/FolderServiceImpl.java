package com.ase.dms.services;

import com.ase.dms.entities.FolderEntity;
import com.ase.dms.exceptions.FolderNotFoundException;
import com.ase.dms.helpers.NameIncrementHelper;
import com.ase.dms.helpers.UuidValidator;
import com.ase.dms.repositories.FolderRepository;
import com.ase.dms.security.UserInformationJWT;
import com.ase.dms.services.UserClient;

import jakarta.validation.ValidationException;

import com.ase.dms.dtos.UserInfoDTO;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.Objects;
import java.util.stream.Collectors;
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
  private final UserClient userClient;

  /**
   * Constructor for FolderServiceImpl.
   * @param folders the folder repository
   */
  public FolderServiceImpl(final FolderRepository folders, final UserClient userClient) {
    this.folders = Objects.requireNonNull(folders);
    this.userClient = Objects.requireNonNull(userClient);
  }

  /**
   * Retrieves the contents of a folder by ID or 'root'.
   * @param id the folder UUID or 'root'
   * @return FolderEntity with subfolders and documents accessible via JPA relationships
   */
  @Override
  @Transactional(readOnly = true)
  public FolderEntity getFolderContents(final String id) {
    FolderEntity folder;
    if (ROOT_ID.equals(id)) {
      folder = folders.findByNameAndParentIsNull(ROOT_ID)
        .orElseThrow(() -> new FolderNotFoundException("Root folder not found"));
    }
    else {
      UuidValidator.validateOrThrow(id);
      folder = folders.findById(id)
        .orElseThrow(() -> new FolderNotFoundException("Ordner "+ id + " nicht gefunden"));
    }

    // Get Cohort from users API
    String cohort = userClient.fetchCurrentUser()
                  .map(UserInfoDTO::getCohort)
                  .orElse(null);

    // Recursively filter subtree if student role
    if (!UserInformationJWT.hasRole("Area-2.Team-7.ReadUpdateDelete.readwrite-document")) {
      folder.setSubfolders(filterFolderTree(folder.getSubfolders(), cohort));
    }
    
    // JPA relationships automatically provides access to subfolders and documents
    return folder;
  }

  // visibility rule: empty = public. Else has to contain consort
  private boolean isVisibleForCohort(FolderEntity f, String cohort) {
    Set<String> groups = f.getStudyGroupIds(); // Field is non-null (emptySet = public)
    if (groups == null || groups.isEmpty()) {
      return true;
    }
    return cohort != null && groups.contains(cohort);
  }

  // recursive filter
  private List<FolderEntity> filterFolderTree(List<FolderEntity> children, String cohort) {
    if (children == null || children.isEmpty()) {
      return List.of();
    }
    return children.stream()
      .filter(f -> isVisibleForCohort(f, cohort))
      .map(f -> {
        f.setSubfolders(filterFolderTree(f.getSubfolders(), cohort));
        return f;
      })
      .collect(Collectors.toList());
  }

  /**
   * Creates a new folder.
   * @param folder the folder entity to create
   * @return the created FolderEntity
   */
  @Override
  @Transactional
  public FolderEntity createFolder(final FolderEntity folder) {
    // Parent Id has to be set
    if (folder.getParentId() == null) {
      throw new ValidationException("parentId must not be null");
    }

    //studyGroupIds normalization - must not be null
    if (folder.getStudyGroupIds() == null) {
      folder.setStudyGroupIds(new HashSet<>());
    }

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
   * JPA will automatically delete all subfolders and documents due to cascade configuration.
   * @param id the folder UUID
   */
  @Override
  @Transactional
  public void deleteFolder(final String id) {
    UuidValidator.validateOrThrow(id);
    if (!folders.existsById(id)) {
      throw new FolderNotFoundException(id);
    }

    // With JPA cascade operations, deleting the folder will automatically
    // delete all subfolders and documents - no warnings
    folders.deleteById(id);
  }
}
