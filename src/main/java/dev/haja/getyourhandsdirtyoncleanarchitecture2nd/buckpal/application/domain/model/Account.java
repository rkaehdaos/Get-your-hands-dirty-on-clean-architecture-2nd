package dev.haja.getyourhandsdirtyoncleanarchitecture2nd.buckpal.application.domain.model;

import lombok.*;

@Getter @ToString
@AllArgsConstructor
public class Account {
    private AccountId id;
    private Money baselineBalance;
    private ActivityWindow activityWindow;

    public Money calculateBalance() {
        return Money.add(
                this.baselineBalance,
                this.activityWindow.calculateBalance(this.id));
    }

    public boolean withdraw(Money money, AccountId targetAccountId) {
        if (!mayWithdraw(money))
            return false;
        return true;
    }

        private boolean mayWithdraw(Money money) {
        return Money.add(
                        this.calculateBalance(),
                        money.negate())
                .isPositiveOrZero();
    }

    @Value
    public static class AccountId {
        Long value;
    }
}
