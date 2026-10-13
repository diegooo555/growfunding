package com.g7.crowdfunding.shared.domain.model;

import com.g7.crowdfunding.shared.domain.exception.InvalidMoneyException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Immutable monetary amount with a currency. Amounts are kept with a scale of 2.
 */
public final class Money implements Comparable<Money> {

    public static final String DEFAULT_CURRENCY = "USD";
    private static final Pattern CURRENCY_PATTERN = Pattern.compile("[A-Z]{3}");
    private static final int SCALE = 2;

    private final BigDecimal amount;
    private final String currency;

    private Money(BigDecimal amount, String currency) {
        if (amount == null) {
            throw new InvalidMoneyException("Amount is required");
        }
        if (currency == null || !CURRENCY_PATTERN.matcher(currency).matches()) {
            throw new InvalidMoneyException("Currency must be a 3-letter ISO code");
        }
        if (amount.stripTrailingZeros().scale() > SCALE) {
            throw new InvalidMoneyException("Amount supports at most 2 decimal places");
        }
        this.amount = amount.setScale(SCALE, RoundingMode.UNNECESSARY);
        this.currency = currency;
    }

    public static Money of(BigDecimal amount, String currency) {
        return new Money(amount, currency);
    }

    public static Money of(String amount, String currency) {
        return new Money(new BigDecimal(amount), currency);
    }

    public static Money zero(String currency) {
        return new Money(BigDecimal.ZERO, currency);
    }

    public BigDecimal amount() {
        return amount;
    }

    public String currency() {
        return currency;
    }

    public boolean isPositive() {
        return amount.signum() > 0;
    }

    public Money add(Money other) {
        requireSameCurrency(other);
        return new Money(amount.add(other.amount), currency);
    }

    public boolean isGreaterThanOrEqual(Money other) {
        requireSameCurrency(other);
        return amount.compareTo(other.amount) >= 0;
    }

    public boolean hasSameCurrencyAs(Money other) {
        return currency.equals(other.currency);
    }

    private void requireSameCurrency(Money other) {
        if (!hasSameCurrencyAs(other)) {
            throw new InvalidMoneyException("Currency mismatch: " + currency + " vs " + other.currency);
        }
    }

    @Override
    public int compareTo(Money other) {
        requireSameCurrency(other);
        return amount.compareTo(other.amount);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Money money)) return false;
        return amount.compareTo(money.amount) == 0 && currency.equals(money.currency);
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount.stripTrailingZeros(), currency);
    }

    @Override
    public String toString() {
        return amount.toPlainString() + " " + currency;
    }
}
