package com.ase.dms.exceptions;

public class MinIODeleteObjectDataException extends DmsException {

  public MinIODeleteObjectDataException(String documentId) {
    super(ErrorCodes.SYS_INTERNAL_ERROR,
        "Document with ID '" + documentId + "' could not be deleted.");
  }

  public MinIODeleteObjectDataException(String documentId, Throwable cause) {
    super(ErrorCodes.SYS_INTERNAL_ERROR,
        "Document with ID '" + documentId + "' could not be deleted.",
        cause);
  }
}
