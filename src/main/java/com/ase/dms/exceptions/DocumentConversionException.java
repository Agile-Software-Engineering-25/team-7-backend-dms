package com.ase.dms.exceptions;

/**
 * Exception thrown when document upload fails
 */
public class DocumentConversionException extends DmsException {

    public DocumentConversionException(String message) {
        super(ErrorCodes.DOC_PDF_CONVERSION_FAILED,
              "Document pdf conversion failed: " + message);
    }

    public DocumentConversionException(String message, Throwable cause) {
        super(ErrorCodes.DOC_PDF_CONVERSION_FAILED,
              "Document pdf conversion failed: " + message,
              cause);
    }
}
