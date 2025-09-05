package com.ase.dms.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when document upload fails
 */
public class DocumentUploadException extends DmsException {

    public DocumentUploadException(String message) {
        super(ErrorCodes.DOC_UPLOAD_FAILED,
              "Document upload failed: " + message,
              HttpStatus.BAD_REQUEST.value());
    }

    public DocumentUploadException(String message, Throwable cause) {
        super(ErrorCodes.DOC_UPLOAD_FAILED,
              "Document upload failed: " + message,
              HttpStatus.BAD_REQUEST.value(),
              cause);
    }
}
