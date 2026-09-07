package dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.model;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MoneyTest {

    @Nested
    class 생성 {

        @Test
        void of로_생성하면_BigInteger_amount를_가진다() {
            Money money = Money.of(1000L);

            assertThat(money.amount()).isEqualTo(BigInteger.valueOf(1000L));
        }

        @Test
        void ZERO는_0값을_가진다() {
            assertThat(Money.ZERO.amount()).isEqualTo(BigInteger.ZERO);
        }

        @Test
        void amount가_null이면_예외가_발생한다() {
            assertThatThrownBy(() -> new Money(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    class 정적_연산 {

        @Test
        void add는_두_Money의_amount를_합산한다() {
            Money result = Money.add(Money.of(1000L), Money.of(500L));

            assertThat(result).isEqualTo(Money.of(1500L));
        }

        @Test
        void subtract는_두_Money의_amount를_뺀다() {
            Money result = Money.subtract(Money.of(1000L), Money.of(1500L));

            assertThat(result).isEqualTo(Money.of(-500L));
        }
    }

    @Nested
    class 인스턴스_연산 {

        @Test
        void plus는_add와_동일한_결과를_반환한다() {
            Money a = Money.of(1000L);
            Money b = Money.of(500L);

            assertThat(a.plus(b)).isEqualTo(Money.add(a, b));
        }

        @Test
        void minus는_subtract와_동일한_결과를_반환한다() {
            Money a = Money.of(1000L);
            Money b = Money.of(1500L);

            assertThat(a.minus(b)).isEqualTo(Money.subtract(a, b));
        }
    }

    @Nested
    class negate {

        @Test
        void 양수는_음수가_된다() {
            assertThat(Money.of(1000L).negate()).isEqualTo(Money.of(-1000L));
        }

        @Test
        void 음수는_양수가_된다() {
            assertThat(Money.of(-1000L).negate()).isEqualTo(Money.of(1000L));
        }

        @Test
        void 영은_영이다() {
            assertThat(Money.ZERO.negate()).isEqualTo(Money.ZERO);
        }
    }

    @Nested
    class 부호_판별 {

        @Test
        void isPositive는_양수일때만_true다() {
            assertThat(Money.of(1L).isPositive()).isTrue();
            assertThat(Money.ZERO.isPositive()).isFalse();
            assertThat(Money.of(-1L).isPositive()).isFalse();
        }

        @Test
        void isPositiveOrZero는_음수일때만_false다() {
            assertThat(Money.of(1L).isPositiveOrZero()).isTrue();
            assertThat(Money.ZERO.isPositiveOrZero()).isTrue();
            assertThat(Money.of(-1L).isPositiveOrZero()).isFalse();
        }

        @Test
        void isNegative는_음수일때만_true다() {
            assertThat(Money.of(-1L).isNegative()).isTrue();
            assertThat(Money.ZERO.isNegative()).isFalse();
            assertThat(Money.of(1L).isNegative()).isFalse();
        }

        @Test
        void isNegativeOrZero는_양수일때만_false다() {
            assertThat(Money.of(-1L).isNegativeOrZero()).isTrue();
            assertThat(Money.ZERO.isNegativeOrZero()).isTrue();
            assertThat(Money.of(1L).isNegativeOrZero()).isFalse();
        }
    }

    @Nested
    class 크기_비교 {

        @Test
        void isGreaterThan은_클때만_true이고_같을때는_false다() {
            assertThat(Money.of(1000L).isGreaterThan(Money.of(500L))).isTrue();
            assertThat(Money.of(1000L).isGreaterThan(Money.of(1000L))).isFalse();
            assertThat(Money.of(500L).isGreaterThan(Money.of(1000L))).isFalse();
        }

        @Test
        void isGreaterThanOrEqualTo는_크거나_같을때_true다() {
            assertThat(Money.of(1000L).isGreaterThanOrEqualTo(Money.of(500L))).isTrue();
            assertThat(Money.of(1000L).isGreaterThanOrEqualTo(Money.of(1000L))).isTrue();
            assertThat(Money.of(500L).isGreaterThanOrEqualTo(Money.of(1000L))).isFalse();
        }
    }
}
