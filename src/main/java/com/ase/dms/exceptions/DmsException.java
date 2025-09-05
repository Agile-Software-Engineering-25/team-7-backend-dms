package com.ase.dms.exceptions;

import lombok.Getter;

/**
 * Base exception class for all DMS-related exceptions
 * Provides error codes and consistent error handling structure
 */
@Getter
public abstract class DmsException extends RuntimeException {

    private final String errorCode;
    private final int httpStatus;

    protected DmsException(String errorCode, String message, int httpStatus) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }

    protected DmsException(String errorCode, String message, int httpStatus, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }
}
