package com.ase.dms.exceptions;

public class MinIOGetObjectDataException extends DmsException {

  public MinIOGetObjectDataException(String documentId) {
    super(ErrorCodes.SYS_INTERNAL_ERROR,
        "Document with ID '" + documentId + "' could not be retrieved.");
  }

  public MinIOGetObjectDataException(String documentId, Throwable cause) {
    super(ErrorCodes.SYS_INTERNAL_ERROR,
        "Document with ID '" + documentId + "' could not be retrieved.",
        cause);
  }
}
