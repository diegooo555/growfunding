package com.g7.crowdfunding.campaign.domain.exception;

import com.g7.crowdfunding.shared.domain.exception.DomainException;

public class ContributionAfterDeadlineException extends DomainException {

    public ContributionAfterDeadlineException() {
        super("DEADLINE_PASSED", "The campaign deadline has passed; contributions are no longer accepted");
    }
}
