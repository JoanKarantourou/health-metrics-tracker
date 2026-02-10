package com.healthmetrics.tracker.exception;

/**
 * Exception thrown when a requested resource is not found in the database.
 *
 * Examples:
 * - Facility with ID 999 doesn't exist
 * - Health indicator with code "XYZ" not found
 */
public class ResourceNotFoundException extends RuntimeException {

    /**
     * Constructor with error message only.
     *
     * @param message Description of what resource was not found
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }

    /**
     * Constructor with error message and cause.
     * Useful when this exception wraps another exception.
     *
     * @param message Description of what resource was not found
     * @param cause The underlying exception that caused this
     */
    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}