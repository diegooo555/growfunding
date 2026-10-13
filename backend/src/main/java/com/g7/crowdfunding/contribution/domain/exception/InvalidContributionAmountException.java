package com.g7.crowdfunding.contribution.domain.exception;

import com.g7.crowdfunding.shared.domain.exception.DomainException;

public class InvalidContributionAmountException extends DomainException {

    public InvalidContributionAmountException(String message) {
        super("INVALID_CONTRIBUTION_AMOUNT", message);
    }
}
