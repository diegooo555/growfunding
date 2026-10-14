package com.g7.crowdfunding.contribution.application.exception;

import com.g7.crowdfunding.shared.application.exception.AccessDeniedException;

public class ContributorsNotVisibleException extends AccessDeniedException {

    public ContributorsNotVisibleException() {
        super("The creator of this campaign has chosen to keep its contributors private");
    }
}
