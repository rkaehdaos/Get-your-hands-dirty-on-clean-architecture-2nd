package dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.model;

import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.model.Account.AccountId;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AccountTest {

    private static final AccountId ACCOUNT_A = new AccountId(1L);
    private static final AccountId ACCOUNT_B = new AccountId(2L);
    private static final LocalDateTime T1 = LocalDateTime.of(2026, 9, 7, 10, 0);
    private static final LocalDateTime T2 = LocalDateTime.of(2026, 9, 7, 11, 0);

    private static Activity 입금활동(LocalDateTime timestamp, long amount) {
        return new Activity(ACCOUNT_A, ACCOUNT_B, ACCOUNT_A, timestamp, Money.of(amount));
    }

    private static Activity 출금활동(LocalDateTime timestamp, long amount) {
        return new Activity(ACCOUNT_A, ACCOUNT_A, ACCOUNT_B, timestamp, Money.of(amount));
    }

    private static Account account(long baselineBalance, Activity... activities) {
        return Account.withId(ACCOUNT_A, Money.of(baselineBalance), new ActivityWindow(activities));
    }

    @Nested
    class AccountId_생성 {

        @Test
        void value를_그대로_보관한다() {
            AccountId id = new AccountId(1L);

            assertThat(id.value()).isEqualTo(1L);
        }

        @Test
        void 같은_value면_동등하다() {
            assertThat(new AccountId(1L)).isEqualTo(new AccountId(1L));
        }

        @Test
        void value가_null이면_예외가_발생한다() {
            assertThatThrownBy(() -> new AccountId(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("value");
        }
    }

    @Nested
    class 잔액_계산 {

        @Test
        void 활동이_없으면_기준_잔액이_그대로_잔액이_된다() {
            Account account = account(500L);

            assertThat(account.calculateBalance()).isEqualTo(Money.of(500L));
        }

        @Test
        void 입금_활동은_기준_잔액에_더해진다() {
            Account account = account(500L, 입금활동(T1, 100L), 입금활동(T2, 200L));

            assertThat(account.calculateBalance()).isEqualTo(Money.of(800L));
        }

        @Test
        void 출금_활동은_기준_잔액에서_빠진다() {
            Account account = account(500L, 출금활동(T1, 100L), 출금활동(T2, 200L));

            assertThat(account.calculateBalance()).isEqualTo(Money.of(200L));
        }

        @Test
        void 입금과_출금이_섞이면_차액이_기준_잔액에_반영된다() {
            Account account = account(500L, 입금활동(T1, 300L), 출금활동(T2, 100L));

            assertThat(account.calculateBalance()).isEqualTo(Money.of(700L));
        }

        @Test
        void 기준_잔액이_ZERO여도_활동만으로_잔액이_계산된다() {
            Account account = account(0L, 입금활동(T1, 300L));

            assertThat(account.calculateBalance()).isEqualTo(Money.of(300L));
        }

        @Test
        void 출금이_기준_잔액을_넘으면_잔액이_음수가_된다() {
            Account account = account(100L, 출금활동(T1, 300L));

            assertThat(account.calculateBalance()).isEqualTo(Money.of(-200L));
        }
    }

    @Nested
    class 출금 {

        @Test
        void 잔액보다_적은_금액은_출금에_성공한다() {
            Account account = account(500L);

            assertThat(account.withdraw(Money.of(300L), ACCOUNT_B)).isTrue();
        }

        @Test
        void 출금에_성공하면_잔액이_그만큼_줄어든다() {
            Account account = account(500L);

            account.withdraw(Money.of(300L), ACCOUNT_B);

            assertThat(account.calculateBalance()).isEqualTo(Money.of(200L));
        }

        @Test
        void 잔액과_같은_금액은_출금에_성공한다() {
            Account account = account(500L);

            assertThat(account.withdraw(Money.of(500L), ACCOUNT_B)).isTrue();
        }

        @Test
        void 잔액_전액을_출금하면_잔액이_ZERO가_된다() {
            Account account = account(500L);

            account.withdraw(Money.of(500L), ACCOUNT_B);

            assertThat(account.calculateBalance()).isEqualTo(Money.ZERO);
        }

        @Test
        void 잔액보다_많은_금액은_출금에_실패한다() {
            Account account = account(500L);

            assertThat(account.withdraw(Money.of(600L), ACCOUNT_B)).isFalse();
        }

        @Test
        void 출금에_실패하면_잔액이_변하지_않는다() {
            Account account = account(500L);

            account.withdraw(Money.of(600L), ACCOUNT_B);

            assertThat(account.calculateBalance()).isEqualTo(Money.of(500L));
        }

        @Test
        void 기존_활동으로_잔액이_늘어난_만큼_더_출금할_수_있다() {
            Account account = account(500L, 입금활동(T1, 300L));

            assertThat(account.withdraw(Money.of(700L), ACCOUNT_B)).isTrue();
        }

        @Test
        void 연속_출금은_직전_출금이_반영된_잔액을_기준으로_판단한다() {
            Account account = account(500L);

            account.withdraw(Money.of(400L), ACCOUNT_B);

            assertThat(account.withdraw(Money.of(400L), ACCOUNT_B)).isFalse();
        }
    }

    @Nested
    class 입금 {

        @Test
        void 입금은_성공한다() {
            Account account = account(500L);

            assertThat(account.deposit(Money.of(300L), ACCOUNT_B)).isTrue();
        }

        @Test
        void 입금하면_잔액이_그만큼_늘어난다() {
            Account account = account(500L);

            account.deposit(Money.of(300L), ACCOUNT_B);

            assertThat(account.calculateBalance()).isEqualTo(Money.of(800L));
        }

        @Test
        void 잔액이_음수여도_입금할_수_있다() {
            Account account = account(0L, 출금활동(T1, 300L));

            account.deposit(Money.of(500L), ACCOUNT_B);

            assertThat(account.calculateBalance()).isEqualTo(Money.of(200L));
        }

        @Test
        void 입금한_금액만큼_더_출금할_수_있게_된다() {
            Account account = account(500L);

            account.deposit(Money.of(300L), ACCOUNT_B);

            assertThat(account.withdraw(Money.of(700L), ACCOUNT_B)).isTrue();
        }
    }
}
