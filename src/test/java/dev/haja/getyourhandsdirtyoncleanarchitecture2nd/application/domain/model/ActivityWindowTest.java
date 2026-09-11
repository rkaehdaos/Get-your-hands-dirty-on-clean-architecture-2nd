package dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.model;

import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.model.Account.AccountId;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static dev.haja.getyourhandsdirtyoncleanarchitecture2nd.common.ActivityTestData.defaultActivity;
import static org.assertj.core.api.Assertions.assertThat;

class ActivityWindowTest {

    @Test
    void calculatesStartTimestamp() {
        ActivityWindow window = new ActivityWindow(
                defaultActivity().withTimestamp(startDate()).build(),
                defaultActivity().withTimestamp(inBetweenDate()).build(),
                defaultActivity().withTimestamp(endDate()).build()
        );
        assertThat(window.getStartTimestamp()).isEqualTo(startDate());
    }

    @Test
    void calculatesEndTimestamp() {
        ActivityWindow window = new ActivityWindow(
                defaultActivity().withTimestamp(startDate()).build(),
                defaultActivity().withTimestamp(inBetweenDate()).build(),
                defaultActivity().withTimestamp(endDate()).build()
        );
        assertThat(window.getEndTimestamp()).isEqualTo(endDate());
    }

    @Test
    void calculatesBalance() {
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

        assertThat(window.calculateBalance(account1)).isEqualTo(Money.of(-500));
        assertThat(window.calculateBalance(account2)).isEqualTo(Money.of(500));

    }

    private static @NonNull LocalDateTime endDate() {
        return LocalDateTime.of(2026, 9, 5, 0, 0);
    }

    private static @NonNull LocalDateTime inBetweenDate() {
        return LocalDateTime.of(2026, 9, 4, 0, 0);
    }

    private static @NonNull LocalDateTime startDate() {
        return LocalDateTime.of(2026, 9, 3, 0, 0);
    }
}
