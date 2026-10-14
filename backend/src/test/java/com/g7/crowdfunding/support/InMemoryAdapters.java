package com.g7.crowdfunding.support;

import com.g7.crowdfunding.campaign.application.port.out.CampaignRepository;
import com.g7.crowdfunding.campaign.domain.model.Campaign;
import com.g7.crowdfunding.campaign.domain.model.CampaignSnapshot;
import com.g7.crowdfunding.campaign.domain.model.CampaignStatus;
import com.g7.crowdfunding.contribution.application.port.out.ContributionRepository;
import com.g7.crowdfunding.contribution.domain.model.Contribution;
import com.g7.crowdfunding.shared.application.exception.ConcurrencyConflictException;
import com.g7.crowdfunding.shared.application.port.out.ClockPort;
import com.g7.crowdfunding.shared.application.port.out.DomainEventOutbox;
import com.g7.crowdfunding.shared.application.port.out.TransactionRunner;
import com.g7.crowdfunding.shared.domain.event.DomainEvent;
import com.g7.crowdfunding.user.application.port.out.UserRepository;
import com.g7.crowdfunding.user.domain.model.Role;
import com.g7.crowdfunding.user.domain.model.User;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

/**
 * Hand-written fakes for application-layer tests (no Spring, no database).
 */
public final class InMemoryAdapters {

    private InMemoryAdapters() {
    }

    public static final UUID CREATOR = UUID.fromString("11111111-1111-1111-1111-111111111111");
    public static final UUID CONTRIBUTOR = UUID.fromString("22222222-2222-2222-2222-222222222222");
    public static final UUID OTHER_CONTRIBUTOR = UUID.fromString("33333333-3333-3333-3333-333333333333");
    public static final UUID ADMIN = UUID.fromString("99999999-9999-9999-9999-999999999999");

    public static class MutableClock implements ClockPort {
        private Instant now;

        public MutableClock(Instant now) {
            this.now = now;
        }

        @Override
        public Instant now() {
            return now;
        }

        public void advance(Duration duration) {
            now = now.plus(duration);
        }
    }

    /**
     * Runs work inline; can be told to fail the next N commits with a concurrency conflict.
     */
    public static class FakeTransactionRunner implements TransactionRunner {
        private final AtomicInteger conflictsToThrow = new AtomicInteger();
        private final AtomicInteger executions = new AtomicInteger();

        public void failNextWithConflict(int times) {
            conflictsToThrow.set(times);
        }

        public int executions() {
            return executions.get();
        }

        @Override
        public <T> T inTransaction(Supplier<T> work) {
            executions.incrementAndGet();
            if (conflictsToThrow.getAndUpdate(n -> Math.max(0, n - 1)) > 0) {
                throw new ConcurrencyConflictException("simulated", null);
            }
            return work.get();
        }
    }

    public static class InMemoryCampaignRepository implements CampaignRepository {
        private final Map<UUID, CampaignSnapshot> store = new ConcurrentHashMap<>();

        @Override
        public Optional<Campaign> findById(UUID id) {
            return Optional.ofNullable(store.get(id)).map(Campaign::rehydrate);
        }

        @Override
        public synchronized void save(Campaign campaign) {
            CampaignSnapshot s = campaign.snapshot();
            CampaignSnapshot current = store.get(s.id());
            Long currentVersion = current == null ? null : current.version();
            if (!Objects.equals(currentVersion, s.version())) {
                throw new ConcurrencyConflictException("version mismatch", null);
            }
            long next = currentVersion == null ? 0 : currentVersion + 1;
            store.put(s.id(), new CampaignSnapshot(s.id(), s.creatorId(), s.title(), s.description(), s.category(),
                    s.goalAmount(), s.currency(), s.deadline(), s.status(), s.raisedAmount(),
                    s.contributionsCount(), s.contributorsCount(), s.contributorsVisibility(), s.createdAt(),
                    s.publishedAt(), s.closedAt(), s.goalReachedAt(), s.closingSoonNotified(), s.milestones(), next));
        }

        @Override
        public List<UUID> findActiveIdsWithDeadlineAtOrBefore(Instant instant) {
            return store.values().stream()
                    .filter(s -> s.status() == CampaignStatus.ACTIVE && !s.deadline().isAfter(instant))
                    .map(CampaignSnapshot::id).toList();
        }

        @Override
        public List<UUID> findActiveIdsPendingClosingSoonNotice(Instant deadlineUpTo) {
            return store.values().stream()
                    .filter(s -> s.status() == CampaignStatus.ACTIVE && !s.closingSoonNotified()
                            && !s.deadline().isAfter(deadlineUpTo))
                    .map(CampaignSnapshot::id).toList();
        }
    }

    public static class InMemoryContributionRepository implements ContributionRepository {
        private final Map<UUID, Contribution> byKey = new ConcurrentHashMap<>();

        @Override
        public Optional<Contribution> findByIdempotencyKey(UUID idempotencyKey) {
            return Optional.ofNullable(byKey.get(idempotencyKey));
        }

        @Override
        public boolean existsByCampaignIdAndContributorId(UUID campaignId, UUID contributorId) {
            return byKey.values().stream().anyMatch(c -> c.belongsTo(campaignId, contributorId));
        }

        @Override
        public void save(Contribution contribution) {
            if (byKey.putIfAbsent(contribution.idempotencyKey(), contribution) != null) {
                throw new IllegalStateException("duplicate idempotency key");
            }
        }

        public int count() {
            return byKey.size();
        }
    }

    public static class RecordingOutbox implements DomainEventOutbox {
        private final List<DomainEvent> events = new ArrayList<>();

        @Override
        public synchronized void append(Collection<? extends DomainEvent> newEvents) {
            events.addAll(newEvents);
        }

        public synchronized List<DomainEvent> events() {
            return List.copyOf(events);
        }

        public <E extends DomainEvent> List<E> eventsOf(Class<E> type) {
            return events().stream().filter(type::isInstance).map(type::cast).toList();
        }
    }

    public static class InMemoryUserRepository implements UserRepository {
        private final Map<UUID, User> users = new ConcurrentHashMap<>();

        public InMemoryUserRepository() {
            add(new User(CREATOR, "Creator", "creator@test", Set.of(Role.CREATOR, Role.CONTRIBUTOR)));
            add(new User(CONTRIBUTOR, "Contributor", "contributor@test", Set.of(Role.CONTRIBUTOR)));
            add(new User(OTHER_CONTRIBUTOR, "Other", "other@test", Set.of(Role.CONTRIBUTOR)));
            add(new User(ADMIN, "Admin", "admin@test", Set.of(Role.ADMIN)));
        }

        public void add(User user) {
            users.put(user.id(), user);
        }

        @Override
        public Optional<User> findById(UUID id) {
            return Optional.ofNullable(users.get(id));
        }

        @Override
        public List<User> findAll() {
            return List.copyOf(users.values());
        }
    }
}
