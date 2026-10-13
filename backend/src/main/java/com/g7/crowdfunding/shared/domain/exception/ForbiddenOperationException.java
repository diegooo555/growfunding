package com.g7.crowdfunding.shared.domain.exception;

/**
 * Raised when the requester is not allowed to perform an operation on an aggregate.
 */
public class ForbiddenOperationException extends DomainException {

    public ForbiddenOperationException(String message) {
        super("FORBIDDEN", message);
    }
}
