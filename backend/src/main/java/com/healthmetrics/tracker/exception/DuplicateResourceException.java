package com.healthmetrics.tracker.exception;

/**
 * Exception thrown when attempting to create a resource that already exists.
 *
 * Examples:
 * - Trying to create a facility with code "FAC001" when it already exists
 * - Attempting to submit duplicate data for the same period
 *
 * This is more specific than ValidationException and helps with clearer error handling.
 */
public class DuplicateResourceException extends RuntimeException {

    /**
     * Constructor with error message only.
     *
     * @param message Description of what duplicate was found
     */
    public DuplicateResourceException(String message) {
        super(message);
    }

    /**
     * Constructor with error message and cause.
     *
     * @param message Description of what duplicate was found
     * @param cause The underlying exception that caused this
     */
    public DuplicateResourceException(String message, Throwable cause) {
        super(message, cause);
    }
}