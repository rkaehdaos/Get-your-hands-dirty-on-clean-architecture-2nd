package dev.haja.getyourhandsdirtyoncleanarchitecture2nd.buckpal.application.domain.model;

import lombok.*;

import java.time.LocalDateTime;

@Getter @ToString
@AllArgsConstructor
public class Account {
    private AccountId id;
    private Money baselineBalance;
    private ActivityWindow activityWindow;

    /**
     * 계정의 총 잔액을 계산한다.
     *
     * @return Money 계정의 총 잔액
     */
    public Money calculateBalance() {
        return Money.add(
                this.baselineBalance,
                this.activityWindow.calculateBalance(this.id));
    }


    // TODO: 이부분임.
    public boolean withdraw(Money money, AccountId targetAccountId) {
        if (!mayWithdraw(money)) return false;
        return true;
    }

    /**
     * 출금 가능 여부를 확인한다.
     *
     * @param money 출금 금액
     * @return 출금 가능 여부
     */
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
