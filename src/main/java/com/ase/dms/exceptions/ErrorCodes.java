package com.ase.dms.exceptions;

/**
 * Error codes used throughout the DMS application
 * Format: DOMAIN_ERROR_TYPE (e.g., DOC_NOT_FOUND, FOLDER_INVALID_NAME)
 */
public final class ErrorCodes {

    // Document-related errors (DOC_*)
    public static final String DOC_NOT_FOUND = "DOC_NOT_FOUND";
    public static final String DOC_UPLOAD_FAILED = "DOC_UPLOAD_FAILED";
    public static final String DOC_INVALID_TYPE = "DOC_INVALID_TYPE";
    public static final String DOC_SIZE_EXCEEDED = "DOC_SIZE_EXCEEDED";
    public static final String DOC_ACCESS_DENIED = "DOC_ACCESS_DENIED";

    // Folder-related errors (FOLDER_*)
    public static final String FOLDER_NOT_FOUND = "FOLDER_NOT_FOUND";
    public static final String FOLDER_INVALID_NAME = "FOLDER_INVALID_NAME";
    public static final String FOLDER_NOT_EMPTY = "FOLDER_NOT_EMPTY";
    public static final String FOLDER_PARENT_INVALID = "FOLDER_PARENT_INVALID";
    public static final String FOLDER_CIRCULAR_REFERENCE = "FOLDER_CIRCULAR_REFERENCE";

    // Validation errors (VAL_*)
    public static final String VAL_INVALID_UUID = "VAL_INVALID_UUID";
    public static final String VAL_MISSING_PARAMETER = "VAL_MISSING_PARAMETER";
    public static final String VAL_INVALID_FILE_TYPE = "VAL_INVALID_FILE_TYPE";

    // System errors (SYS_*)
    public static final String SYS_DATABASE_ERROR = "SYS_DATABASE_ERROR";
    public static final String SYS_INTERNAL_ERROR = "SYS_INTERNAL_ERROR";

    private ErrorCodes() {
        // Utility class - prevent instantiation
    }
}
