package com.ase.dms.exceptions;

/**
 * Exception thrown when a folder is not found
 */
public class FolderNotFoundException extends DmsException {

    public FolderNotFoundException(String folderId) {
        super(ErrorCodes.FOLDER_NOT_FOUND,
              "Folder with ID '" + folderId + "' was not found");
    }

    public FolderNotFoundException(String folderId, Throwable cause) {
        super(ErrorCodes.FOLDER_NOT_FOUND,
              "Folder with ID '" + folderId + "' was not found",
              cause);
    }
}
