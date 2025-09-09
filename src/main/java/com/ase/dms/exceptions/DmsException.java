package com.ase.dms.exceptions;

import lombok.Getter;

/**
 * Base exception class for all DMS-related exceptions
 * Now uses ErrorCodes enum for type safety and automatic HTTP status mapping
 */
@Getter
public abstract class DmsException extends RuntimeException {

  /**
   * -- GETTER --
   *  Get the error code enum
   */
  private final ErrorCodes errorCode;

    protected DmsException(ErrorCodes errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    protected DmsException(ErrorCodes errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

  /**
     * Get the error code as string (equivalent to errorCode.name())
     */
    public String getErrorCodeString() {
        return errorCode.name();
    }

    /**
     * Get the HTTP status code from the error code enum
     */
    public int getHttpStatus() {
        return errorCode.getHttpStatusValue();
    }
}
