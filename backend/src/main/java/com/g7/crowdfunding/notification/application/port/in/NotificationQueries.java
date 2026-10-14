package com.g7.crowdfunding.notification.application.port.in;

import com.g7.crowdfunding.notification.application.query.NotificationView;

import java.util.List;
import java.util.UUID;

public interface NotificationQueries {

    List<NotificationView> notificationsOf(UUID userId, UUID requesterId);
}
