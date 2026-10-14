package com.g7.crowdfunding.shared.application.exception;

/**
 * Signals a lost optimistic-locking race; callers may retry the whole unit of work.
 */
public class ConcurrencyConflictException extends ConflictException {

    public ConcurrencyConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}
