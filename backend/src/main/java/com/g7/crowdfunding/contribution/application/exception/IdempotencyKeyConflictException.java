package com.g7.crowdfunding.contribution.application.exception;

import com.g7.crowdfunding.shared.application.exception.ConflictException;

public class IdempotencyKeyConflictException extends ConflictException {

    public IdempotencyKeyConflictException() {
        super("The idempotency key was already used for a different contribution request");
    }
}
