package dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

import static java.util.Objects.requireNonNull;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Account {

    private AccountId id;
    private Money baselineBalance;
    @Getter private ActivityWindow activityWindow;

    /**
     * Creates an {@link Account} entity without an ID. Use to create a new entity that is not yet
     * persisted.
     */
    public static Account withoutId(
            Money baselineBalance,
            ActivityWindow activityWindow) {
        return new Account(null, baselineBalance, activityWindow);
    }
    /**
     * Creates an {@link Account} entity with an ID. Use to reconstitute a persisted entity.
     */
    public static Account withId(
            AccountId accountId,
            Money baselineBalance,
            ActivityWindow activityWindow) {
        return new Account(accountId, baselineBalance, activityWindow);
    }

    /**
     * 기준 잔액에 거래 내역의 금액을 더하여 계좌의 총 잔액을 계산.
     */
    public Money calculateBalance() {
        return Money.add(
                this.baselineBalance,
                this.activityWindow.calculateBalance(this.id));
    }

    public boolean withdraw(Money money, AccountId targetAccountId) {
        if (!mayWithdraw(money)) return false;

        Activity withdrawal = new Activity(
                this.id,
                this.id,
                targetAccountId,
                LocalDateTime.now(),
                money);
        this.activityWindow = this.activityWindow.addActivity(withdrawal);
        return true;
    }

    /**
     * 출금 후 잔액이 0 이상이면 출금할 수 있다. 즉 잔액 전액 출금은 허용된다.
     */
    private boolean mayWithdraw(Money money) {
        return Money.add(this.calculateBalance(), money.negate()).isPositiveOrZero();
    }

    public boolean deposit(Money money, AccountId sourceAccountId) {
        Activity deposit = new Activity(
                this.id,
                sourceAccountId,
                this.id,
                LocalDateTime.now(),
                money);
        this.activityWindow = this.activityWindow.addActivity(deposit);
        return true;
    }

    /**
     * 식별자가 있는 계좌만 표현한다. "아직 id가 없음"은 AccountId(null)이 아니라
     * AccountId 참조 자체가 null인 것으로만 표현한다.
     * AccountId(null)을 허용하면 서로 다른 미영속 계좌가 equals로 같아진다.
     */
    public record AccountId(Long value) {
        public AccountId {
            requireNonNull(value, "value must not be null");
        }
    }
}
