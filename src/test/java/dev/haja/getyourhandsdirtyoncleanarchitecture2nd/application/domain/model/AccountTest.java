package dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.model;

import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.model.Account.AccountId;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static dev.haja.getyourhandsdirtyoncleanarchitecture2nd.common.AccountTestData.defaultAccount;
import static dev.haja.getyourhandsdirtyoncleanarchitecture2nd.common.ActivityTestData.defaultActivity;
import static org.assertj.core.api.Assertions.assertThat;


class AccountTest {

    // 도메인이 현재 시각을 읽지 않으므로 활동의 시각은 테스트가 정한다
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 9, 12, 12, 0);

    @Test
    void calculatesBalance() {

        // given
        AccountId accountId = new AccountId(1L);
        Account account = defaultAccount()
                .withAccountId(accountId)
                .withBaselineBalance(Money.of(555L))
                .withActivityWindow(new ActivityWindow(
                        defaultActivity()
                                .withTargetAccount(accountId)
                                .withMoney(Money.of(999L)).build(),
                        defaultActivity()
                                .withTargetAccount(accountId)
                                .withMoney(Money.of(1L)).build()))
                .build();

        // when
        Money balance = account.calculateBalance();

        // then
        assertThat(balance).isEqualTo(Money.of(1555L));
    }

    @Test
    void withdrawalSucceeds() {

        // given
        AccountId accountId = new AccountId(1L);
        Account account = defaultAccount()
                .withAccountId(accountId)
                .withBaselineBalance(Money.of(555L))
                .withActivityWindow(new ActivityWindow(
                        defaultActivity()
                                .withTargetAccount(accountId)
                                .withMoney(Money.of(999L)).build(),
                        defaultActivity()
                                .withTargetAccount(accountId)
                                .withMoney(Money.of(1L)).build()))
                .build();

        // when
        AccountId randomTargetAccount = new AccountId(99L);
        boolean success = account.withdraw(Money.of(555L), randomTargetAccount, NOW);

        // then
        assertThat(success).isTrue();
        assertThat(account
                .getActivityWindow()
                .activities())
                .hasSize(3);
        assertThat(account.calculateBalance())
                .isEqualTo(Money.of(1000L));

        // 넘긴 시각이 새 활동에 그대로 기록된다
        assertThat(account.getActivityWindow().activities().getLast().timestamp())
                .isEqualTo(NOW);
    }

    @Test
    void withdrawalFailure() {

        // given
        AccountId accountId = new AccountId(1L);
        Account account = defaultAccount()
                .withAccountId(accountId)
                .withBaselineBalance(Money.of(555L))
                .withActivityWindow(new ActivityWindow(
                        defaultActivity()
                                .withTargetAccount(accountId)
                                .withMoney(Money.of(999L)).build(),
                        defaultActivity()
                                .withTargetAccount(accountId)
                                .withMoney(Money.of(1L)).build()))
                .build();

        // when
        boolean success = account.withdraw(Money.of(1556L), new AccountId(99L), NOW);

        // then
        assertThat(success).isFalse();
        assertThat(account
                .getActivityWindow()
                .activities())
                .hasSize(2);
        assertThat(account.calculateBalance())
                .isEqualTo(Money.of(1555L));
    }

    @Test
    void depositSuccess() {

        // given
        AccountId accountId = new AccountId(1L);
        Account account = defaultAccount()
                .withAccountId(accountId)
                .withBaselineBalance(Money.of(555L))
                .withActivityWindow(new ActivityWindow(
                        defaultActivity()
                                .withTargetAccount(accountId)
                                .withMoney(Money.of(999L)).build(),
                        defaultActivity()
                                .withTargetAccount(accountId)
                                .withMoney(Money.of(1L)).build()))
                .build();

        // when
        boolean success = account.deposit(Money.of(445L), new AccountId(99L), NOW);

        // then
        assertThat(success).isTrue();
        assertThat(account.
                getActivityWindow()
                .activities())
                .hasSize(3);
        assertThat(account.calculateBalance())
                .isEqualTo(Money.of(2000L));

        // 넘긴 시각이 새 활동에 그대로 기록된다
        assertThat(account.getActivityWindow().activities().getLast().timestamp())
                .isEqualTo(NOW);
    }
}
