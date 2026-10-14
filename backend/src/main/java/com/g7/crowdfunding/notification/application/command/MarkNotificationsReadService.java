package com.g7.crowdfunding.notification.application.command;

import com.g7.crowdfunding.notification.application.port.in.MarkNotificationsReadUseCase;
import com.g7.crowdfunding.notification.application.port.out.NotificationRepository;
import com.g7.crowdfunding.notification.application.NotificationAccessPolicy;
import com.g7.crowdfunding.shared.application.port.out.TransactionRunner;
import com.g7.crowdfunding.user.application.UserDirectory;

import java.util.UUID;

public class MarkNotificationsReadService implements MarkNotificationsReadUseCase {

    private final NotificationRepository repository;
    private final UserDirectory userDirectory;
    private final TransactionRunner tx;

    public MarkNotificationsReadService(NotificationRepository repository, UserDirectory userDirectory,
                                        TransactionRunner tx) {
        this.repository = repository;
        this.userDirectory = userDirectory;
        this.tx = tx;
    }

    @Override
    public int markAllRead(UUID userId, UUID requesterId) {
        NotificationAccessPolicy.requireSelfOrAdmin(userDirectory, userId, requesterId);
        return tx.inTransaction(() -> repository.markAllRead(userId));
    }
}
