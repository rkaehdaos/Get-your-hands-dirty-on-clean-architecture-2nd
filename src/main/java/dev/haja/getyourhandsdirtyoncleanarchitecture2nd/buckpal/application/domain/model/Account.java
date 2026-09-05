package dev.haja.getyourhandsdirtyoncleanarchitecture2nd.buckpal.application.domain.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigInteger;
import java.util.Optional;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Account {
    private final AccountId id;
    private final Money baselineBalance;
    @Getter
    final ActivityWindow activityWindow;

    public static Account withoutId(
            Money baselineBalance,
            ActivityWindow activityWindow) {
        return new Account(null, baselineBalance, activityWindow);
    }

    public static Account withId(
            AccountId accountId,
            Money baselineBalance,
            ActivityWindow activityWindow) {
        return new Account(accountId, baselineBalance, activityWindow);
    }

    public Optional<AccountId> getId() {
        return Optional.ofNullable(this.id);
    }

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

    /**
     * 출금 가능 여부를 확인
     */
    private boolean mayWithdraw(Money money) {
        return Money.add(
                        this.calculateBalance(),
                        money.negate())
                .isPositiveOrZero();
    }

    public record AccountId(Long value) {}
}
