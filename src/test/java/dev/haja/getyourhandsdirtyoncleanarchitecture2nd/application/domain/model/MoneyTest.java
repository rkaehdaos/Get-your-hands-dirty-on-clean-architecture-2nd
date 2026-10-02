package dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MoneyTest {

    private static final Money NEGATIVE = Money.of(-1L);
    private static final Money POSITIVE = Money.of(1L);

    @Test
    void isPositive() {

        // when / then
        assertThat(NEGATIVE.isPositive()).isFalse();
        assertThat(Money.ZERO.isPositive()).isFalse();
        assertThat(POSITIVE.isPositive()).isTrue();
    }

    @Test
    void isPositiveOrZero() {

        // when / then
        assertThat(NEGATIVE.isPositiveOrZero()).isFalse();
        assertThat(Money.ZERO.isPositiveOrZero()).isTrue();
        assertThat(POSITIVE.isPositiveOrZero()).isTrue();
    }

    @Test
    void isNegative() {

        // when / then
        assertThat(NEGATIVE.isNegative()).isTrue();
        assertThat(Money.ZERO.isNegative()).isFalse();
        assertThat(POSITIVE.isNegative()).isFalse();
    }

    @Test
    void isNegativeOrZero() {

        // when / then
        assertThat(NEGATIVE.isNegativeOrZero()).isTrue();
        assertThat(Money.ZERO.isNegativeOrZero()).isTrue();
        assertThat(POSITIVE.isNegativeOrZero()).isFalse();
    }

    @Test
    void isGreaterThan() {

        // when / then
        assertThat(Money.ZERO.isGreaterThan(POSITIVE)).isFalse();
        assertThat(Money.ZERO.isGreaterThan(Money.ZERO)).isFalse();
        assertThat(Money.ZERO.isGreaterThan(NEGATIVE)).isTrue();
    }

    @Test
    void isGreaterThanOrEqualTo() {

        // when / then
        assertThat(Money.ZERO.isGreaterThanOrEqualTo(POSITIVE)).isFalse();
        assertThat(Money.ZERO.isGreaterThanOrEqualTo(Money.ZERO)).isTrue();
        assertThat(Money.ZERO.isGreaterThanOrEqualTo(NEGATIVE)).isTrue();
    }

}
