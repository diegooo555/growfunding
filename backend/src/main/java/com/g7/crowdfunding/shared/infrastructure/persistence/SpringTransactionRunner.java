package com.g7.crowdfunding.shared.infrastructure.persistence;

import com.g7.crowdfunding.shared.application.exception.ConcurrencyConflictException;
import com.g7.crowdfunding.shared.application.port.out.TransactionRunner;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Locale;
import java.util.function.Supplier;

@Component
public class SpringTransactionRunner implements TransactionRunner {

    private static final String IDEMPOTENCY_CONSTRAINT = "uk_contributions_idempotency_key";

    private final TransactionTemplate template;

    public SpringTransactionRunner(PlatformTransactionManager transactionManager) {
        this.template = new TransactionTemplate(transactionManager);
    }

    @Override
    public <T> T inTransaction(Supplier<T> work) {
        try {
            return template.execute(status -> work.get());
        } catch (OptimisticLockingFailureException e) {
            throw new ConcurrencyConflictException("The resource was modified concurrently", e);
        } catch (DataIntegrityViolationException e) {
            if (isIdempotencyRace(e)) {
                throw new ConcurrencyConflictException("A request with the same idempotency key is in progress", e);
            }
            throw e;
        }
    }

    private static boolean isIdempotencyRace(DataIntegrityViolationException e) {
        String message = e.getMostSpecificCause().getMessage();
        return message != null && message.toLowerCase(Locale.ROOT).contains(IDEMPOTENCY_CONSTRAINT);
    }
}
