package com.g7.stats.infrastructure.persistence;

import com.g7.stats.domain.CampaignStats;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CampaignStatsRepository extends JpaRepository<CampaignStats, UUID> {

    List<CampaignStats> findTop10ByOrderByRaisedDesc();
}
