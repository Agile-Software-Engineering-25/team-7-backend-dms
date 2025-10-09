package com.ase.dms.exceptions;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Error codes used throughout the DMS application with HTTP status mappings
 * Using enum provides type safety and automatic string conversion via .name()
 */
@Getter
@AllArgsConstructor
public enum ErrorCodes {
    //HTTP Error Codes Quick Reference
    //400 Bad Request - The server could not understand the request due to invalid syntax.
    //401 Unauthorized - The client must authenticate itself to get the requested response.
    //403 Forbidden - The client does not have access rights to the content.
    //404 Not Found - The server can not find the requested resource.
    //409 Conflict - The request could not be completed due to a conflict with the current state
    //413 Payload Too Large - The request is larger than the server is willing or able to process.
    //500 Internal Server Error - The server has encountered a situation it doesn't know how to handle

    // Document-related errors (DOC_*)
    DOC_NOT_FOUND(HttpStatus.NOT_FOUND),
    DOC_UPLOAD_FAILED(HttpStatus.BAD_REQUEST),
    DOC_INVALID_TYPE(HttpStatus.BAD_REQUEST),
    DOC_SIZE_EXCEEDED(HttpStatus.PAYLOAD_TOO_LARGE),
    DOC_ACCESS_DENIED(HttpStatus.FORBIDDEN),

    // Folder-related errors (FOLDER_*)
    FOLDER_NOT_FOUND(HttpStatus.NOT_FOUND),
    FOLDER_INVALID_NAME(HttpStatus.BAD_REQUEST),
    FOLDER_NOT_EMPTY(HttpStatus.BAD_REQUEST),
    FOLDER_PARENT_INVALID(HttpStatus.BAD_REQUEST),
    FOLDER_CIRCULAR_REFERENCE(HttpStatus.BAD_REQUEST),

    // Validation errors (VAL_*)
    VAL_INVALID_UUID(HttpStatus.BAD_REQUEST),
    VAL_MISSING_PARAMETER(HttpStatus.BAD_REQUEST),
    VAL_INVALID_FILE_TYPE(HttpStatus.BAD_REQUEST),
    VAL_CHILDREN_FOLDER(HttpStatus.BAD_REQUEST),

  // System errors (SYS_*)
    SYS_INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR);

  private final HttpStatus httpStatus;

    /**
     * Get the HTTP status code associated with this error
     */
    public int getHttpStatusValue() {
        return httpStatus.value();
    }
}
