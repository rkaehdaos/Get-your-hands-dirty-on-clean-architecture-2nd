package dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.model;

import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.model.Account.AccountId;
import org.junit.jupiter.api.Test;

import static dev.haja.getyourhandsdirtyoncleanarchitecture2nd.common.AccountTestData.defaultAccount;
import static dev.haja.getyourhandsdirtyoncleanarchitecture2nd.common.ActivityTestData.defaultActivity;
import static org.assertj.core.api.Assertions.assertThat;

//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.BDDMockito.then;

class AccountTest {

    @Test
    void withdrawalSucceeds() {
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

        AccountId randomTargetAccount = new AccountId(99L);
        boolean success = account.withdraw(Money.of(555L), randomTargetAccount);

        assertThat(success).isTrue();
        assertThat(account
                .getActivityWindow()
                .activities())
                .hasSize(3);
        assertThat(account.calculateBalance())
                .isEqualTo(Money.of(1000L));
    }

    @Test
    void withdrawalFailure() {
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

        boolean success = account.withdraw(Money.of(1556L), new AccountId(99L));

        assertThat(success).isFalse();
        assertThat(account
                .getActivityWindow()
                .activities())
                .hasSize(2);
        assertThat(account.calculateBalance())
                .isEqualTo(Money.of(1555L));
    }
}
