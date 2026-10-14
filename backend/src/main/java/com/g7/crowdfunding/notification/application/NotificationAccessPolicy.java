package com.g7.crowdfunding.notification.application;

import com.g7.crowdfunding.shared.application.exception.AccessDeniedException;
import com.g7.crowdfunding.user.application.UserDirectory;
import com.g7.crowdfunding.user.domain.model.User;

import java.util.UUID;

/**
 * A user may only access their own notifications, unless they are an admin.
 */
public final class NotificationAccessPolicy {

    private NotificationAccessPolicy() {
    }

    public static void requireSelfOrAdmin(UserDirectory directory, UUID userId, UUID requesterId) {
        User requester = directory.require(requesterId);
        if (!requester.id().equals(userId) && !requester.isAdmin()) {
            throw new AccessDeniedException("You can only access your own notifications");
        }
        directory.require(userId);
    }
}
