package com.igsl.opsfinder.exception;

/**
 * Thrown when an API key exceeds its configured rate limit.
 */
public class RateLimitExceededException extends RuntimeException {

    public RateLimitExceededException(String message) {
        super(message);
    }
}
