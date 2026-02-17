package com.healthmetrics.tracker.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Standard error response structure sent to clients when exceptions occur.
 * Provides consistent error format across all API endpoints.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private String path;
    private Map<String, String> fieldErrors;

    /**
     * Constructor for creating error responses with automatic timestamp.
     *
     * @param status HTTP status code (e.g., 404, 400, 500)
     * @param error Error type (e.g., "Not Found", "Bad Request")
     * @param message Detailed error message
     * @param path The API endpoint that caused the error
     */
    public ErrorResponse(int status, String error, String message, String path) {
        this.timestamp = LocalDateTime.now();
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
    }
}
