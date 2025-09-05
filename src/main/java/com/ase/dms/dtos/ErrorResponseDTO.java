 package com.ase.dms.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Standardized error response format for all API errors
 */
@Getter
@Setter
@Schema(description = "Standard error response format")
public class ErrorResponseDTO {

    @Schema(description = "Error code for programmatic handling",
            example = "DOC_NOT_FOUND")
    private String errorCode;

    @Schema(description = "Human-readable error message",
            example = "Document with ID '123' was not found")
    private String message;

    @Schema(description = "HTTP status code",
            example = "404")
    private int status;

    @Schema(description = "Request path where error occurred",
            example = "/dms/v1/documents/123")
    private String path;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    @Schema(description = "Timestamp when error occurred",
            example = "2025-01-05T10:30:00Z")
    private LocalDateTime timestamp;

    @Schema(description = "Additional error details (validation errors, etc.)",
            example = "{\"fieldName\": \"Field is required\"}")
    private Map<String, Object> details;

    public ErrorResponseDTO() {
        this.timestamp = LocalDateTime.now();
    }

    public ErrorResponseDTO(String errorCode, String message, int status, String path) {
        this();
        this.errorCode = errorCode;
        this.message = message;
        this.status = status;
        this.path = path;
    }
}
