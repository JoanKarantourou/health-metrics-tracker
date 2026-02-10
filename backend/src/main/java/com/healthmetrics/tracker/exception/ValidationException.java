package com.healthmetrics.tracker.exception;

/**
 * Exception thrown when validation rules are violated.
 *
 * Examples:
 * - Facility code already exists (duplicate)
 * - Period end date is before period start date
 * - Value is negative when it should be positive
 */
public class ValidationException extends RuntimeException {

    /**
     * Constructor with error message only.
     *
     * @param message Description of the validation error
     */
    public ValidationException(String message) {
        super(message);
    }

    /**
     * Constructor with error message and cause.
     *
     * @param message Description of the validation error
     * @param cause The underlying exception that caused this
     */
    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}