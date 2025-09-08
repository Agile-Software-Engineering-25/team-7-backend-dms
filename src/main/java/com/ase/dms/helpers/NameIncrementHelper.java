package com.ase.dms.helpers;

import com.ase.dms.entities.DocumentEntity;
import com.ase.dms.entities.FolderEntity;

import java.util.Collection;
import java.util.Set;

public class NameIncrementHelper {
  /**
   * Returns a unique name by adding an increment suffix if the name already exists in the same directory.
   *
   * @param baseName      Original name
   * @param existingNames Set of all names in the directory
   * @return Unique name with increment suffix if needed
   */
  public static String getIncrementedName(String baseName, Set<String> existingNames) {
    if (!existingNames.contains(baseName)) {
      return baseName;
    }
    int counter = 1;
    String newName;
    int dotIndex = baseName.lastIndexOf('.');
    String namePart = dotIndex > 0 ? baseName.substring(0, dotIndex) : baseName;
    String extPart = dotIndex > 0 ? baseName.substring(dotIndex) : "";
    do {
      newName = namePart + " (" + counter + ")" + extPart;
      counter++;
    } while (existingNames.contains(newName));
    return newName;
  }

  /**
   * Collects all names of entities (folders or documents) that share the same parent or folder ID.
   * Optionally excludes the entity with the given excludeId (useful for update operations).
   * Supports both FolderEntity (using parentId) and DocumentEntity (using folderId).
   *
   * @param entities         Collection of FolderEntity or DocumentEntity objects
   * @param parentOrFolderId The parentId (for folders) or folderId (for documents) to match
   * @param excludeId        The ID to exclude from the result (can be null)
   * @return Set of names of sibling entities in the same directory/folder
   */
  public static Set<String> collectSiblingNames(Collection<?> entities, String parentOrFolderId, String excludeId) {
    Set<String> names = new java.util.HashSet<>();
    for (Object obj : entities) {
      if (obj instanceof FolderEntity f) {
        if (f.getParentId() != null && f.getParentId().equals(parentOrFolderId)
            && f.getId() != null && !f.getId().equals(excludeId)) {
          names.add(f.getName());
        }
      }
      else if (obj instanceof DocumentEntity d) {
        if (d.getFolderId().equals(parentOrFolderId) && !d.getId().equals(excludeId)) {
          names.add(d.getName());
        }
      }
    }
    return names;
  }
}
