package com.g7.crowdfunding.shared.domain;

import com.g7.crowdfunding.shared.domain.exception.InvalidMoneyException;
import com.g7.crowdfunding.shared.domain.model.Money;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MoneyTest {

    @Test
    void addsAmountsOfTheSameCurrency() {
        assertThat(Money.of("10.25", "USD").add(Money.of("0.75", "USD"))).isEqualTo(Money.of("11", "USD"));
    }

    @Test
    void rejectsCurrencyMismatch() {
        assertThatThrownBy(() -> Money.of("1", "USD").add(Money.of("1", "EUR")))
                .isInstanceOf(InvalidMoneyException.class);
    }

    @Test
    void rejectsMoreThanTwoDecimals() {
        assertThatThrownBy(() -> Money.of("1.001", "USD")).isInstanceOf(InvalidMoneyException.class);
    }

    @Test
    void rejectsInvalidCurrencyCode() {
        assertThatThrownBy(() -> Money.of("1", "usd")).isInstanceOf(InvalidMoneyException.class);
    }
}
