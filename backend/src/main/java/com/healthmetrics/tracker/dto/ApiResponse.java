package com.healthmetrics.tracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Generic wrapper for API responses.
 * Provides a consistent structure for all API endpoints.
 * It allows the frontend to handle responses uniformly, whether success or error.
 * Example success response:
 * {
 *   "success": true,
 *   "message": "Facility created successfully",
 *   "data": { ...facility object... },
 *   "timestamp": "2024-02-10T10:30:00"
 * }
 *
 * Example error response:
 * {
 *   "success": false,
 *   "message": "Facility with code 'FAC001' already exists",
 *   "data": null,
 *   "timestamp": "2024-02-10T10:30:00"
 * }
 *
 * @param <T> The type of data being returned (FacilityDTO, List<HealthIndicatorDTO>, etc.)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

    /// Indicates whether the operation was successful.
    /// true = success, false = error/failure
    private boolean success;

    /// Human-readable message about the operation.
    /// Example: "Facility created successfully", "Facility not found"
    private String message;

    /// The actual data payload.
    /// Can be a single object, a list, or null (in case of errors or no data to return).
    /// The generic type T provides type safety.
    private T data;

    /// Timestamp when this response was generated. Useful for debugging and logging.
    private LocalDateTime timestamp;

    /**
     * Factory method to create a success response with data.
     *
     * @param message Success message
     * @param data The data to return
     * @param <T> Type of the data
     * @return ApiResponse with success=true
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data, LocalDateTime.now());
    }

    /**
     * Factory method to create a success response without data.
     * Useful for operations like delete where no data needs to be returned.
     *
     * @param message Success message
     * @param <T> Type parameter (can be Void)
     * @return ApiResponse with success=true and data=null
     */
    public static <T> ApiResponse<T> success(String message) {
        return new ApiResponse<>(true, message, null, LocalDateTime.now());
    }

    /**
     * Factory method to create an error response.
     *
     * @param message Error message explaining what went wrong
     * @param <T> Type parameter (usually matches the expected success response type)
     * @return ApiResponse with success=false and data=null
     */
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null, LocalDateTime.now());
    }

    /**
     * Factory method to create an error response with data.
     * Useful when you want to return additional context with the error.
     *
     * @param message Error message
     * @param data Additional error context (validation errors, etc.)
     * @param <T> Type of the error data
     * @return ApiResponse with success=false
     */
    public static <T> ApiResponse<T> error(String message, T data) {
        return new ApiResponse<>(false, message, data, LocalDateTime.now());
    }
}