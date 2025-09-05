package com.ase.dms.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when a folder is not found
 */
public class FolderNotFoundException extends DmsException {

    public FolderNotFoundException(String folderId) {
        super(ErrorCodes.FOLDER_NOT_FOUND,
              "Folder with ID '" + folderId + "' was not found",
              HttpStatus.NOT_FOUND.value());
    }

    public FolderNotFoundException(String message, String errorCode) {
        super(errorCode != null ? errorCode : ErrorCodes.FOLDER_NOT_FOUND,
              message,
              HttpStatus.NOT_FOUND.value());
    }

    public FolderNotFoundException(String folderId, Throwable cause) {
        super(ErrorCodes.FOLDER_NOT_FOUND,
              "Folder with ID '" + folderId + "' was not found",
              HttpStatus.NOT_FOUND.value(),
              cause);
    }
}
