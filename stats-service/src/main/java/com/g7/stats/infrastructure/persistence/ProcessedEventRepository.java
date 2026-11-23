package com.g7.stats.infrastructure.persistence;

import com.g7.stats.domain.ProcessedEvent;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProcessedEventRepository extends JpaRepository<ProcessedEvent, UUID> {
}
