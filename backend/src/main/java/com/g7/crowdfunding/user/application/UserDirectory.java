package com.g7.crowdfunding.user.application;

import com.g7.crowdfunding.shared.application.exception.AccessDeniedException;
import com.g7.crowdfunding.shared.application.exception.ResourceNotFoundException;
import com.g7.crowdfunding.user.application.port.out.UserRepository;
import com.g7.crowdfunding.user.domain.model.Role;
import com.g7.crowdfunding.user.domain.model.User;

import java.util.UUID;

/**
 * Resolves and authorizes the acting user for other modules' use cases.
 */
public class UserDirectory {

    private final UserRepository users;

    public UserDirectory(UserRepository users) {
        this.users = users;
    }

    public User require(UUID userId) {
        if (userId == null) {
            throw new AccessDeniedException("A user identity (X-User-Id) is required");
        }
        return users.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User", userId));
    }

    public User requireRole(UUID userId, Role role) {
        User user = require(userId);
        if (!user.hasRole(role)) {
            throw new AccessDeniedException("User " + userId + " lacks role " + role);
        }
        return user;
    }
}
