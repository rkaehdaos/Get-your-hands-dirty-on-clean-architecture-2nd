package dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.model;

import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.model.Account.AccountId;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static dev.haja.getyourhandsdirtyoncleanarchitecture2nd.common.ActivityTestData.defaultActivity;
import static org.assertj.core.api.Assertions.assertThat;

class ActivityWindowTest {

    @Test
    void calculatesStartTimestamp() {

        // given
        ActivityWindow window = new ActivityWindow(
                defaultActivity().withTimestamp(startDate()).build(),
                defaultActivity().withTimestamp(inBetweenDate()).build(),
                defaultActivity().withTimestamp(endDate()).build()
        );

        // when
        LocalDateTime startTimestamp = window.getStartTimestamp();

        // then
        assertThat(startTimestamp).isEqualTo(startDate());
    }

    @Test
    void calculatesEndTimestamp() {

        // given
        ActivityWindow window = new ActivityWindow(
                defaultActivity().withTimestamp(startDate()).build(),
                defaultActivity().withTimestamp(inBetweenDate()).build(),
                defaultActivity().withTimestamp(endDate()).build()
        );

        // when
        LocalDateTime endTimestamp = window.getEndTimestamp();

        // then
        assertThat(endTimestamp).isEqualTo(endDate());
    }

    @Test
    void calculatesBalance() {

        // given
        AccountId account1 = new AccountId(1L);
        AccountId account2 = new AccountId(2L);

        ActivityWindow window = new ActivityWindow(
                defaultActivity()
                        .withSourceAccount(account1)
                        .withTargetAccount(account2)
                        .withMoney(Money.of(999)).build(),
                defaultActivity()
                        .withSourceAccount(account1)
                        .withTargetAccount(account2)
                        .withMoney(Money.of(1)).build(),
                defaultActivity()
                        .withSourceAccount(account2)
                        .withTargetAccount(account1)
                        .withMoney(Money.of(500)).build());

        // when
        Money balanceOfAccount1 = window.calculateBalance(account1);
        Money balanceOfAccount2 = window.calculateBalance(account2);

        // then
        assertThat(balanceOfAccount1).isEqualTo(Money.of(-500));
        assertThat(balanceOfAccount2).isEqualTo(Money.of(500));
    }

    private static LocalDateTime endDate() {
        return LocalDateTime.of(2026, 9, 5, 0, 0);
    }

    private static LocalDateTime inBetweenDate() {
        return LocalDateTime.of(2026, 9, 4, 0, 0);
    }

    private static LocalDateTime startDate() {
        return LocalDateTime.of(2026, 9, 3, 0, 0);
    }
}
