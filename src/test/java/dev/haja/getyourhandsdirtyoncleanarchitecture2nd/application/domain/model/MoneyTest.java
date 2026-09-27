package dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MoneyTest {

    private static final Money NEGATIVE = Money.of(-1L);
    private static final Money ZERO = Money.of(0L);
    private static final Money POSITIVE = Money.of(1L);

    @Test
    void isPositive() {
        assertThat(NEGATIVE.isPositive()).isFalse();
        assertThat(ZERO.isPositive()).isFalse();
        assertThat(POSITIVE.isPositive()).isTrue();
    }

    @Test
    void isPositiveOrZero() {
        assertThat(NEGATIVE.isPositiveOrZero()).isFalse();
        assertThat(ZERO.isPositiveOrZero()).isTrue();
        assertThat(POSITIVE.isPositiveOrZero()).isTrue();
    }

    @Test
    void isNegative() {
        assertThat(NEGATIVE.isNegative()).isTrue();
        assertThat(ZERO.isNegative()).isFalse();
        assertThat(POSITIVE.isNegative()).isFalse();
    }

    @Test
    void isNegativeOrZero() {
        assertThat(NEGATIVE.isNegativeOrZero()).isTrue();
        assertThat(ZERO.isNegativeOrZero()).isTrue();
        assertThat(POSITIVE.isNegativeOrZero()).isFalse();
    }

    @Test
    void isGreaterThan() {
        assertThat(ZERO.isGreaterThan(POSITIVE)).isFalse();
        assertThat(ZERO.isGreaterThan(ZERO)).isFalse();
        assertThat(ZERO.isGreaterThan(NEGATIVE)).isTrue();
    }

    @Test
    void isGreaterThanOrEqualTo() {
        assertThat(ZERO.isGreaterThanOrEqualTo(POSITIVE)).isFalse();
        assertThat(ZERO.isGreaterThanOrEqualTo(ZERO)).isTrue();
        assertThat(ZERO.isGreaterThanOrEqualTo(NEGATIVE)).isTrue();
    }

}
