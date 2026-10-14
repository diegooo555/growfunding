package com.g7.crowdfunding.campaign.application.command;

import com.g7.crowdfunding.campaign.application.port.in.ProcessCampaignDeadlinesUseCase;
import com.g7.crowdfunding.campaign.application.port.out.CampaignRepository;
import com.g7.crowdfunding.campaign.domain.model.Campaign;
import com.g7.crowdfunding.shared.application.exception.ConcurrencyConflictException;
import com.g7.crowdfunding.shared.application.port.out.ClockPort;
import com.g7.crowdfunding.shared.application.port.out.DomainEventOutbox;
import com.g7.crowdfunding.shared.application.port.out.TransactionRunner;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.function.BiPredicate;

/**
 * HU-06: closes campaigns past their deadline and emits ClosingSoon notices. Each campaign is
 * processed in its own transaction so a single failure does not block the rest; a lost
 * optimistic-locking race is simply retried on the next scheduler tick.
 */
public class CampaignDeadlineService implements ProcessCampaignDeadlinesUseCase {

    private static final System.Logger LOG = System.getLogger(CampaignDeadlineService.class.getName());

    private final CampaignRepository campaigns;
    private final DomainEventOutbox outbox;
    private final TransactionRunner tx;
    private final ClockPort clock;
    private final Duration closingSoonWindow;

    public CampaignDeadlineService(CampaignRepository campaigns, DomainEventOutbox outbox, TransactionRunner tx,
                                   ClockPort clock, Duration closingSoonWindow) {
        this.campaigns = campaigns;
        this.outbox = outbox;
        this.tx = tx;
        this.clock = clock;
        this.closingSoonWindow = closingSoonWindow;
    }

    @Override
    public int closeExpiredCampaigns() {
        Instant now = clock.now();
        int closed = 0;
        for (UUID id : campaigns.findActiveIdsWithDeadlineAtOrBefore(now)) {
            if (process(id, now, (campaign, at) -> campaign.closeByDeadline(at))) {
                closed++;
            }
        }
        return closed;
    }

    @Override
    public int flagCampaignsClosingSoon() {
        Instant now = clock.now();
        int flagged = 0;
        for (UUID id : campaigns.findActiveIdsPendingClosingSoonNotice(now.plus(closingSoonWindow))) {
            if (process(id, now, (campaign, at) -> campaign.markClosingSoon(at, closingSoonWindow))) {
                flagged++;
            }
        }
        return flagged;
    }

    private boolean process(UUID id, Instant now, BiPredicate<Campaign, Instant> action) {
        try {
            return tx.inTransaction(() -> {
                Campaign campaign = campaigns.findById(id).orElse(null);
                if (campaign == null || !action.test(campaign, now)) {
                    return false;
                }
                campaigns.save(campaign);
                outbox.append(campaign.pullDomainEvents());
                return true;
            });
        } catch (ConcurrencyConflictException e) {
            LOG.log(System.Logger.Level.INFO, "Campaign {0} changed concurrently; will retry next run", id);
            return false;
        }
    }
}
