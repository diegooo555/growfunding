package com.g7.crowdfunding.contribution.domain.exception;

import com.g7.crowdfunding.shared.domain.exception.DomainException;

public class InvalidContributionException extends DomainException {

    public InvalidContributionException(String message) {
        super("INVALID_CONTRIBUTION", message);
    }
}
