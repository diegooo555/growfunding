package com.g7.crowdfunding.shared.application.port.out;

import java.util.function.Supplier;

/**
 * Unit-of-work boundary. Keeps the application layer free of framework annotations.
 * Implementations must translate optimistic-locking and idempotency races into
 * {@link com.g7.crowdfunding.shared.application.exception.ConcurrencyConflictException}.
 */
public interface TransactionRunner {

    <T> T inTransaction(Supplier<T> work);

    default void inTransaction(Runnable work) {
        inTransaction(() -> {
            work.run();
            return null;
        });
    }
}
