package com.igsl.opsfinder.exception;

/**
 * Exception thrown when client sends a bad request.
 */
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }

    public BadRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
