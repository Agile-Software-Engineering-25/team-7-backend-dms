package com.ase.dms.exceptions;

/**
 * Exception thrown when document upload fails
 */
public class DocumentUploadException extends DmsException {

    public DocumentUploadException(String message) {
        super(ErrorCodes.DOC_UPLOAD_FAILED,
              "Document upload failed: " + message);
    }

    public DocumentUploadException(String message, Throwable cause) {
        super(ErrorCodes.DOC_UPLOAD_FAILED,
              "Document upload failed: " + message,
              cause);
    }
}
