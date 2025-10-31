package com.ase.dms.exceptions;

/**
 * Exception thrown when document upload fails
 */
public class DocumentConversionInternalException extends DmsException {

    public DocumentConversionInternalException(String message) {
        super(ErrorCodes.SYS_INTERNAL_ERROR,
              "Document pdf conversion failed: " + message);
    }

    public DocumentConversionInternalException(String message, Throwable cause) {
        super(ErrorCodes.SYS_INTERNAL_ERROR,
              "Document pdf conversion failed: " + message,
              cause);
    }
}
