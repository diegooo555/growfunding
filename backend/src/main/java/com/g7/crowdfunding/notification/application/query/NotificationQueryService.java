package com.g7.crowdfunding.notification.application.query;

import com.g7.crowdfunding.notification.application.port.in.NotificationQueries;
import com.g7.crowdfunding.notification.application.port.out.NotificationRepository;
import com.g7.crowdfunding.notification.application.NotificationAccessPolicy;
import com.g7.crowdfunding.user.application.UserDirectory;

import java.util.List;
import java.util.UUID;

public class NotificationQueryService implements NotificationQueries {

    private static final int LIMIT = 100;

    private final NotificationRepository repository;
    private final UserDirectory userDirectory;

    public NotificationQueryService(NotificationRepository repository, UserDirectory userDirectory) {
        this.repository = repository;
        this.userDirectory = userDirectory;
    }

    @Override
    public List<NotificationView> notificationsOf(UUID userId, UUID requesterId) {
        NotificationAccessPolicy.requireSelfOrAdmin(userDirectory, userId, requesterId);
        return repository.findByRecipient(userId, LIMIT).stream()
                .map(n -> new NotificationView(n.id(), n.type().name(), n.campaignId(), n.title(), n.message(),
                        n.createdAt(), n.read()))
                .toList();
    }
}
