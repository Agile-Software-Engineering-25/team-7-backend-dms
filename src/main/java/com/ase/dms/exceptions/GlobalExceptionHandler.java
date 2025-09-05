package com.ase.dms.exceptions;

import com.ase.dms.dtos.ErrorResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
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
 * Provides consistent error response format across the application
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handle all DMS-specific exceptions
     */
    @ExceptionHandler(DmsException.class)
    public ResponseEntity<ErrorResponseDTO> handleDmsException(
            DmsException ex, HttpServletRequest request) {

        logger.error("DMS Exception: {} - {}", ex.getErrorCode(), ex.getMessage(), ex);

        ErrorResponseDTO error = new ErrorResponseDTO(
            ex.getErrorCode(),
            ex.getMessage(),
            ex.getHttpStatus(),
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
            HttpStatus.BAD_REQUEST.value(),
            request.getRequestURI()
        );
        error.setDetails(validationErrors);

        logger.warn("Validation error: {}", validationErrors);
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
            HttpStatus.BAD_REQUEST.value(),
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
            HttpStatus.PAYLOAD_TOO_LARGE.value(),
            request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(error);
    }

    /**
     * Handle invalid JSON or request body format
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponseDTO> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, HttpServletRequest request) {

        ErrorResponseDTO error = new ErrorResponseDTO(
            "VAL_INVALID_REQUEST_BODY",
            "Invalid request body format",
            HttpStatus.BAD_REQUEST.value(),
            request.getRequestURI()
        );

        return ResponseEntity.badRequest().body(error);
    }

    /**
     * Handle unsupported HTTP methods
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponseDTO> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {

        ErrorResponseDTO error = new ErrorResponseDTO(
            "HTTP_METHOD_NOT_SUPPORTED",
            "HTTP method '" + ex.getMethod() + "' is not supported for this endpoint",
            HttpStatus.METHOD_NOT_ALLOWED.value(),
            request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(error);
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
            HttpStatus.BAD_REQUEST.value(),
            request.getRequestURI()
        );

        return ResponseEntity.badRequest().body(error);
    }

    /**
     * Handle constraint violations (Bean Validation)
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponseDTO> handleConstraintViolation(
            ConstraintViolationException ex, HttpServletRequest request) {

        Map<String, Object> violations = new HashMap<>();
        ex.getConstraintViolations().forEach(violation ->
            violations.put(violation.getPropertyPath().toString(), violation.getMessage())
        );

        ErrorResponseDTO error = new ErrorResponseDTO(
            "VAL_CONSTRAINT_VIOLATION",
            "Validation constraints violated",
            HttpStatus.BAD_REQUEST.value(),
            request.getRequestURI()
        );
        error.setDetails(violations);

        return ResponseEntity.badRequest().body(error);
    }

    /**
     * Handle all other unexpected exceptions
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGenericException(
            Exception ex, HttpServletRequest request) {

        logger.error("Unexpected error occurred", ex);

        ErrorResponseDTO error = new ErrorResponseDTO(
            ErrorCodes.SYS_INTERNAL_ERROR,
            "An internal server error occurred",
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            request.getRequestURI()
        );

        return ResponseEntity.internalServerError().body(error);
    }
}
