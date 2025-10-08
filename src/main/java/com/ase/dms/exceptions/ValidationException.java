package com.ase.dms.exceptions;

/**
 * Exception thrown for validation errors
 */
public class ValidationException extends DmsException {

    public ValidationException(ErrorCodes errorCode, String message) {
        super(errorCode, message);
    }
}
