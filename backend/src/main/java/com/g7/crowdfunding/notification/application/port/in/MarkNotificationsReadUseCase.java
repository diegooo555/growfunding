package com.g7.crowdfunding.notification.application.port.in;

import java.util.UUID;

public interface MarkNotificationsReadUseCase {

    int markAllRead(UUID userId, UUID requesterId);
}
