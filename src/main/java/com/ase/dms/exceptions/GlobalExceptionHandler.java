package com.ase.dms.exceptions;

import com.ase.dms.dtos.ErrorResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for all DMS controllers
 * Now uses ErrorCodes enum for cleaner error handling
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handle all DMS-specific exceptions using ErrorCodes enum
     */
    @ExceptionHandler(DmsException.class)
    public ResponseEntity<ErrorResponseDTO> handleDmsException(
            DmsException ex, HttpServletRequest request) {

        LOGGER.error("DMS Exception: {} - {}", ex.getErrorCodeString(), ex.getMessage(), ex);

        // Use the new convenience constructor with ErrorCodes enum
        ErrorResponseDTO error = new ErrorResponseDTO(
            ex.getErrorCode(),
            ex.getMessage(),
            request.getRequestURI()
        );

        return ResponseEntity.status(ex.getHttpStatus()).body(error);
    }

    /**
     * Handle validation errors from @Valid annotations
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleValidationException(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        Map<String, Object> validationErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
            validationErrors.put(error.getField(), error.getDefaultMessage())
        );

        ErrorResponseDTO error = new ErrorResponseDTO(
            ErrorCodes.VAL_MISSING_PARAMETER,
            "Validation failed",
            request.getRequestURI()
        );
        error.setDetails(validationErrors);

        LOGGER.warn("Validation error: {}", validationErrors);
        return ResponseEntity.badRequest().body(error);
    }

    /**
     * Handle missing request parameters
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponseDTO> handleMissingParameterException(
            MissingServletRequestParameterException ex, HttpServletRequest request) {

        ErrorResponseDTO error = new ErrorResponseDTO(
            ErrorCodes.VAL_MISSING_PARAMETER,
            "Required parameter '" + ex.getParameterName() + "' is missing",
            request.getRequestURI()
        );

        return ResponseEntity.badRequest().body(error);
    }

    /**
     * Handle file upload size exceeded
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponseDTO> handleMaxUploadSizeException(
            MaxUploadSizeExceededException ex, HttpServletRequest request) {

        ErrorResponseDTO error = new ErrorResponseDTO(
            ErrorCodes.DOC_SIZE_EXCEEDED,
            "File size exceeds maximum allowed size",
            request.getRequestURI()
        );

        return ResponseEntity.status(ErrorCodes.DOC_SIZE_EXCEEDED.getHttpStatus()).body(error);
    }

    /**
     * Handle parameter type mismatch (e.g., invalid UUID format)
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponseDTO> handleTypeMismatchException(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {

        ErrorResponseDTO error = new ErrorResponseDTO(
            ErrorCodes.VAL_INVALID_UUID,
            "Invalid parameter format: '" + ex.getValue() + "'",
            request.getRequestURI()
        );

        return ResponseEntity.badRequest().body(error);
    }

    /**
     * Handle all other unexpected exceptions
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGenericException(
            Exception ex, HttpServletRequest request) {

        LOGGER.error("Unexpected error occurred", ex);

        ErrorResponseDTO error = new ErrorResponseDTO(
            ErrorCodes.SYS_INTERNAL_ERROR,
            "An internal server error occurred",
            request.getRequestURI()
        );

        return ResponseEntity.internalServerError().body(error);
    }
}
