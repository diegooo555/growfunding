package com.g7.crowdfunding.shared.application.exception;

/**
 * The request conflicts with the current state of a resource.
 */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }

    public ConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}
