package com.tripstack.exception;

/**
 * Thrown when user-entered form data fails validation
 * (missing required field, invalid number, invalid date, etc.).
 * Caught by the UI layer and shown to the user as an inline message.
 */
public class ValidationException extends Exception {

    public ValidationException(String message) {
        super(message);
    }
}
