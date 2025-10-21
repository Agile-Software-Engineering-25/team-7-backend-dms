package com.ase.dms.exceptions;

public class MinIOSetObjectDataException extends DmsException {

  public MinIOSetObjectDataException(String documentId) {
    super(ErrorCodes.SYS_INTERNAL_ERROR,
        "Document with ID '" + documentId + "' could not be retrieved.");
  }

  public MinIOSetObjectDataException(String documentId, Throwable cause) {
    super(ErrorCodes.SYS_INTERNAL_ERROR,
        "Document with ID '" + documentId + "' could not be uploaded.",
        cause);
  }
}
