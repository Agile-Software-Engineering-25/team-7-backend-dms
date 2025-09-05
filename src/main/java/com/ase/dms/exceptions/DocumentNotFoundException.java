package com.ase.dms.exceptions;

/**
 * Exception thrown when a document is not found
 */
public class DocumentNotFoundException extends DmsException {

    public DocumentNotFoundException(String documentId) {
        super(ErrorCodes.DOC_NOT_FOUND,
              "Document with ID '" + documentId + "' was not found");
    }

    public DocumentNotFoundException(String documentId, Throwable cause) {
        super(ErrorCodes.DOC_NOT_FOUND,
              "Document with ID '" + documentId + "' was not found",
              cause);
    }
}
