package com.g7.crowdfunding.shared.domain.exception;

public class InvalidMoneyException extends DomainException {

    public InvalidMoneyException(String message) {
        super("INVALID_MONEY", message);
    }
}
