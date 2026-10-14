package com.g7.crowdfunding.user.infrastructure.adapter.out.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "users")
public class UserJpaEntity {

    @Id
    UUID id;
    String displayName;
    String email;
    /**
     * Comma-separated role names.
     */
    String roles;

    protected UserJpaEntity() {
    }
}
