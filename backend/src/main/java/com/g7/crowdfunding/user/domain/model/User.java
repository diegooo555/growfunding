package com.g7.crowdfunding.user.domain.model;

import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Platform user. Authentication is out of scope: requests identify the user through
 * the {@code X-User-Id} header.
 */
public final class User {

    private final UUID id;
    private final String displayName;
    private final String email;
    private final Set<Role> roles;

    public User(UUID id, String displayName, String email, Set<Role> roles) {
        this.id = Objects.requireNonNull(id);
        this.displayName = Objects.requireNonNull(displayName);
        this.email = Objects.requireNonNull(email);
        this.roles = roles.isEmpty() ? EnumSet.noneOf(Role.class) : EnumSet.copyOf(roles);
    }

    public UUID id() {
        return id;
    }

    public String displayName() {
        return displayName;
    }

    public String email() {
        return email;
    }

    public Set<Role> roles() {
        return Set.copyOf(roles);
    }

    public boolean hasRole(Role role) {
        return roles.contains(role);
    }

    public boolean isAdmin() {
        return hasRole(Role.ADMIN);
    }
}
