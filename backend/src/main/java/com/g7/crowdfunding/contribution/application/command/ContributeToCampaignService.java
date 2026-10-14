package com.g7.crowdfunding.contribution.application.command;

import com.g7.crowdfunding.campaign.application.port.out.CampaignRepository;
import com.g7.crowdfunding.campaign.domain.model.Campaign;
import com.g7.crowdfunding.contribution.application.exception.IdempotencyKeyConflictException;
import com.g7.crowdfunding.contribution.application.port.in.ContributeToCampaignUseCase;
import com.g7.crowdfunding.contribution.application.port.out.ContributionRepository;
import com.g7.crowdfunding.contribution.domain.model.Contribution;
import com.g7.crowdfunding.shared.application.exception.ConcurrencyConflictException;
import com.g7.crowdfunding.shared.application.exception.ResourceNotFoundException;
import com.g7.crowdfunding.shared.application.port.out.ClockPort;
import com.g7.crowdfunding.shared.application.port.out.DomainEventOutbox;
import com.g7.crowdfunding.shared.application.port.out.TransactionRunner;
import com.g7.crowdfunding.shared.domain.model.Money;
import com.g7.crowdfunding.user.application.UserDirectory;

/**
 * HU-04 / NFR-3. The contribution, the campaign progress update and the outbox events are
 * written in one transaction. Concurrent contributions to the same campaign are serialized by
 * optimistic locking on the aggregate version; the losing transaction is retried. Duplicate
 * requests carrying the same idempotency key return the originally registered contribution.
 */
public class ContributeToCampaignService implements ContributeToCampaignUseCase {

    private final CampaignRepository campaigns;
    private final ContributionRepository contributions;
    private final DomainEventOutbox outbox;
    private final UserDirectory userDirectory;
    private final TransactionRunner tx;
    private final ClockPort clock;
    private final int maxAttempts;

    public ContributeToCampaignService(CampaignRepository campaigns, ContributionRepository contributions,
                                       DomainEventOutbox outbox, UserDirectory userDirectory,
                                       TransactionRunner tx, ClockPort clock, int maxAttempts) {
        this.campaigns = campaigns;
        this.contributions = contributions;
        this.outbox = outbox;
        this.userDirectory = userDirectory;
        this.tx = tx;
        this.clock = clock;
        this.maxAttempts = Math.max(1, maxAttempts);
    }

    @Override
    public ContributionReceipt contribute(ContributeCommand command) {
        userDirectory.require(command.contributorId());
        ConcurrencyConflictException lastConflict = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return tx.inTransaction(() -> attemptContribution(command));
            } catch (ConcurrencyConflictException conflict) {
                lastConflict = conflict;
                backoff(attempt);
            }
        }
        throw lastConflict;
    }

    private ContributionReceipt attemptContribution(ContributeCommand command) {
        var existing = contributions.findByIdempotencyKey(command.idempotencyKey());
        if (existing.isPresent()) {
            return replay(existing.get(), command);
        }
        Campaign campaign = campaigns.findById(command.campaignId())
                .orElseThrow(() -> new ResourceNotFoundException("Campaign", command.campaignId()));
        String currency = command.currency() == null || command.currency().isBlank()
                ? campaign.goal().currency() : command.currency().toUpperCase();
        Money amount = command.amount() == null ? null : Money.of(command.amount(), currency);
        boolean newContributor = !contributions.existsByCampaignIdAndContributorId(
                campaign.id(), command.contributorId());

        Contribution contribution = campaign.contribute(command.contributorId(), amount, command.anonymous(),
                command.message(), command.idempotencyKey(), newContributor, clock.now());

        campaigns.save(campaign);
        contributions.save(contribution);
        outbox.append(campaign.pullDomainEvents());
        return receipt(contribution, campaign, false);
    }

    private ContributionReceipt replay(Contribution existing, ContributeCommand command) {
        if (!existing.belongsTo(command.campaignId(), command.contributorId())) {
            throw new IdempotencyKeyConflictException();
        }
        Campaign campaign = campaigns.findById(existing.campaignId())
                .orElseThrow(() -> new ResourceNotFoundException("Campaign", existing.campaignId()));
        return receipt(existing, campaign, true);
    }

    private static ContributionReceipt receipt(Contribution c, Campaign campaign, boolean replayed) {
        return new ContributionReceipt(c.id(), c.campaignId(), c.amount().amount(), c.amount().currency(),
                c.createdAt(), replayed, campaign.raisedAmount().amount(), campaign.contributionsCount(),
                campaign.contributorsCount(), campaign.isGoalReached());
    }

    private static void backoff(int attempt) {
        try {
            Thread.sleep(15L * attempt);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
