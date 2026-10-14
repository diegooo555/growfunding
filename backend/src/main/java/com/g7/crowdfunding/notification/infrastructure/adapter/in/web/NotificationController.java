package com.g7.crowdfunding.notification.infrastructure.adapter.in.web;

import com.g7.crowdfunding.notification.application.port.in.MarkNotificationsReadUseCase;
import com.g7.crowdfunding.notification.application.port.in.NotificationQueries;
import com.g7.crowdfunding.notification.application.query.NotificationView;
import com.g7.crowdfunding.shared.infrastructure.web.UserHeader;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/users/{userId}/notifications")
class NotificationController {

    private final NotificationQueries queries;
    private final MarkNotificationsReadUseCase markRead;

    NotificationController(NotificationQueries queries, MarkNotificationsReadUseCase markRead) {
        this.queries = queries;
        this.markRead = markRead;
    }

    @GetMapping
    List<NotificationView> list(@PathVariable UUID userId, @RequestHeader(UserHeader.NAME) UUID requesterId) {
        return queries.notificationsOf(userId, requesterId);
    }

    @PostMapping("/read-all")
    Map<String, Integer> readAll(@PathVariable UUID userId, @RequestHeader(UserHeader.NAME) UUID requesterId) {
        return Map.of("updated", markRead.markAllRead(userId, requesterId));
    }
}
