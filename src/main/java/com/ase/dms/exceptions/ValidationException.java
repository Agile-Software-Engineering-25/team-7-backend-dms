package com.ase.dms.exceptions;

/**
 * Exception thrown for validation errors
 */
public class ValidationException extends DmsException {

    public ValidationException(ErrorCodes errorCode, String message) {
        super(errorCode, message);
    }

    public static ValidationException invalidUuid(String value) {
        return new ValidationException(ErrorCodes.VAL_INVALID_UUID,
            "Invalid UUID format: '" + value + "'");
    }

    public static ValidationException missingParameter(String parameter) {
        return new ValidationException(ErrorCodes.VAL_MISSING_PARAMETER,
            "Required parameter '" + parameter + "' is missing");
    }

    public static ValidationException invalidFileType(String fileType) {
        return new ValidationException(ErrorCodes.VAL_INVALID_FILE_TYPE,
            "File type '" + fileType + "' is not allowed");
    }
}
