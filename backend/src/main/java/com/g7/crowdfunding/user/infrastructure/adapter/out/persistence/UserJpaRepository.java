package com.g7.crowdfunding.user.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

interface UserJpaRepository extends JpaRepository<UserJpaEntity, UUID> {
}
