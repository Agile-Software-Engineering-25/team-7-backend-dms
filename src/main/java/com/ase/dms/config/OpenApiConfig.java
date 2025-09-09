package com.ase.dms.config;

import com.ase.dms.exceptions.ErrorCodes;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI configuration for the DMS application
 * Centralizes error response schemas and common API documentation
 */
@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "Document Management System API",
        version = "1.0.0",
        description = """
            A modern REST API for document and folder management.

            ## Error Handling
            All endpoints return standardized error responses with machine-readable error codes.

            ## Error Codes
            - `DOC_*`: Document-related errors
            - `FOLDER_*`: Folder-related errors
            - `VAL_*`: Validation errors
            - `SYS_*`: System errors
            """,
        contact = @Contact(
            name = "DMS Development Team",
            email = "dev-team@example.com"
        ),
        license = @License(
            name = "MIT License",
            url = "https://opensource.org/licenses/MIT"
        )
    ),
    servers = {
        @Server(url = "http://localhost:8080", description = "Development server"),
        @Server(url = "https://api.dms.example.com", description = "Production server")
    }
)
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .components(new Components()
                // Reusable error response schemas
                .addSchemas("ErrorResponse", createErrorResponseSchema())

                // Reusable common error responses
                .addResponses("BadRequestResponse", createBadRequestResponse())
                .addResponses("NotFoundResponse", createNotFoundResponse())
                .addResponses("ValidationErrorResponse", createValidationErrorResponse())
                .addResponses("InternalServerErrorResponse", createInternalServerErrorResponse())
                .addResponses("PayloadTooLargeResponse", createPayloadTooLargeResponse())

                // Document-specific responses
                .addResponses("DocumentNotFoundResponse", createDocumentNotFoundResponse())
                .addResponses("DocumentUploadFailedResponse", createDocumentUploadFailedResponse())

                // Folder-specific responses
                .addResponses("FolderNotFoundResponse", createFolderNotFoundResponse())
            );
    }

    private Schema<?> createErrorResponseSchema() {
        return new Schema<>()
            .type("object")
            .description("Standard error response format")
            .addProperty("errorCode", new Schema<>().type("string"))
            .addProperty("message", new Schema<>().type("string"))
            .addProperty("status", new Schema<>().type("integer"))
            .addProperty("path", new Schema<>().type("string"))
            .addProperty("timestamp", new Schema<>().type("string").format("date-time"))
            .addProperty("details", new Schema<>().type("object").nullable(true));
    }

    // Specific document responses
    private ApiResponse createDocumentNotFoundResponse() {
        return new ApiResponse()
            .description("Document not found")
            .content(new Content()
                .addMediaType("application/json", new MediaType()
                    .schema(new Schema<>().$ref("#/components/schemas/ErrorResponse"))
                    .example(Map.of(
                        "errorCode", "DOC_NOT_FOUND",
                        "message", "Document with ID '2a2d1575-1ba8-446b-a78c-c2035a178588' was not found",
                        "status", ErrorCodes.DOC_NOT_FOUND.getHttpStatusValue(),
                        "timestamp", "2025-01-05T10:30:00Z"
                    ))
                )
            );
    }

    private ApiResponse createDocumentUploadFailedResponse() {
        return new ApiResponse()
            .description("Document upload failed")
            .content(new Content()
                .addMediaType("application/json", new MediaType()
                    .schema(new Schema<>().$ref("#/components/schemas/ErrorResponse"))
                    .example(Map.of(
                        "errorCode", "DOC_UPLOAD_FAILED",
                        "message", "Document upload failed",
                        "status", ErrorCodes.DOC_UPLOAD_FAILED.getHttpStatusValue(),
                        "timestamp", "2025-01-05T10:30:00Z"
                    ))
                )
            );
    }

    private ApiResponse createFolderNotFoundResponse() {
        return new ApiResponse()
            .description("Folder not found")
            .content(new Content()
                .addMediaType("application/json", new MediaType()
                    .schema(new Schema<>().$ref("#/components/schemas/ErrorResponse"))
                    .example(Map.of(
                        "errorCode", "FOLDER_NOT_FOUND",
                        "message", "Folder with ID '2a2d1575-1ba8-446b-a78c-c2035a178588' was not found",
                        "status", ErrorCodes.FOLDER_NOT_FOUND.getHttpStatusValue(),
                        "timestamp", "2025-01-05T10:30:00Z"
                    ))
                )
            );
    }

    private ApiResponse createBadRequestResponse() {
        return new ApiResponse()
            .description("Bad Request - Invalid input data")
            .content(new Content()
                .addMediaType("application/json", new MediaType()
                    .schema(new Schema<>().$ref("#/components/schemas/ErrorResponse"))));
    }

    private ApiResponse createNotFoundResponse() {
        return new ApiResponse()
            .description("Resource not found")
            .content(new Content()
                .addMediaType("application/json", new MediaType()
                    .schema(new Schema<>().$ref("#/components/schemas/ErrorResponse"))));
    }

    private ApiResponse createValidationErrorResponse() {
        return new ApiResponse()
            .description("Validation failed")
            .content(new Content()
                .addMediaType("application/json", new MediaType()
                    .schema(new Schema<>().$ref("#/components/schemas/ErrorResponse"))));
    }

    private ApiResponse createInternalServerErrorResponse() {
        return new ApiResponse()
            .description("Internal server error")
            .content(new Content()
                .addMediaType("application/json", new MediaType()
                    .schema(new Schema<>().$ref("#/components/schemas/ErrorResponse"))));
    }

    private ApiResponse createPayloadTooLargeResponse() {
        return new ApiResponse()
            .description("File size exceeds maximum allowed size")
            .content(new Content()
                .addMediaType("application/json", new MediaType()
                    .schema(new Schema<>().$ref("#/components/schemas/ErrorResponse"))));
    }
}
