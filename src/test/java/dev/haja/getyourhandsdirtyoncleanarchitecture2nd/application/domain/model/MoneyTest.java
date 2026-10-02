package dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

    // 산술의 피연산자는 3과 2다. 순서를 뒤집으면 뺄셈의 결과가 달라진다

    @Test
    void add() {

        // when
        Money sum = Money.add(Money.of(3L), Money.of(2L));

        // then
        assertThat(sum).isEqualTo(Money.of(5L));
    }

    @Test
    void subtract() {

        // when
        Money difference = Money.subtract(Money.of(3L), Money.of(2L));

        // then
        assertThat(difference).isEqualTo(Money.of(1L));
    }

    @Test
    void plus() {

        // when
        Money sum = Money.of(3L).plus(Money.of(2L));

        // then
        assertThat(sum).isEqualTo(Money.of(5L));
    }

    @Test
    void minus() {

        // when
        Money difference = Money.of(3L).minus(Money.of(2L));

        // then
        assertThat(difference).isEqualTo(Money.of(1L));
    }

    @Test
    void negate() {

        // when
        Money negated = Money.of(3L).negate();

        // then
        assertThat(negated).isEqualTo(Money.of(-3L));
    }

    @Test
    @DisplayName("금액이 null이면 Money 생성이 거부됨")
    void givenNullAmount_thenThrowsNullPointerException() {

        // when / then
        assertThatThrownBy(() -> new Money(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("amount");
    }

}
