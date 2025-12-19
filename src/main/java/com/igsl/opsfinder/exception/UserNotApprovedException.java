package com.igsl.opsfinder.exception;

/**
 * Exception thrown when a user attempts to login but has not been approved by an admin.
 */
public class UserNotApprovedException extends RuntimeException {

    public UserNotApprovedException(String message) {
        super(message);
    }

    public UserNotApprovedException(String message, Throwable cause) {
        super(message, cause);
    }
}
