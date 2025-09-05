package com.ase.dms.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when a document is not found
 */
public class DocumentNotFoundException extends DmsException {

    public DocumentNotFoundException(String documentId) {
        super(ErrorCodes.DOC_NOT_FOUND,
              "Document with ID '" + documentId + "' was not found",
              HttpStatus.NOT_FOUND.value());
    }

    public DocumentNotFoundException(String documentId, Throwable cause) {
        super(ErrorCodes.DOC_NOT_FOUND,
              "Document with ID '" + documentId + "' was not found",
              HttpStatus.NOT_FOUND.value(),
              cause);
    }
}
