package com.tripstack.exception;

/**
 * Wraps low-level java.sql.SQLException (and other data-access failures)
 * so the UI layer never has to deal with SQLException directly.
 * Caught by the UI layer and shown to the user as an alert dialog.
 */
public class DataAccessException extends Exception {

    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }

    public DataAccessException(String message) {
        super(message);
    }
}
