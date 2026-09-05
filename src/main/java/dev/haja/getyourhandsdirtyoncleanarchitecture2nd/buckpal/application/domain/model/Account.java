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


    /**
     * 출금: 이 계좌에서 일정 금액을 출금하려고 시도
     *
     * @param money           출금 금액
     * @param targetAccountId 대상 계좌 id
     * @return 출금 성공 여부
     */
    public boolean withdraw(Money money, AccountId targetAccountId) {
        if (!mayWithdraw(money)) return false;

        Activity withdrawal = new Activity(
                this.id,
                this.id,
                targetAccountId,
                LocalDateTime.now(),
                money);
        this.activityWindow.addActivity(withdrawal);
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

    /**
     * 입금: 이 계좌에 일정 금액을 입금하려고 시도
     * 성공하면 양수 값의 새 활동을 생성하고 활동 윈도우에 추가
     *
     * @param money           입금 금액
     * @param sourceAccountId 소스 계좌 id
     * @return 입금 성공 여부
     */
    public boolean deposit(Money money, AccountId sourceAccountId) {
        Activity deposit = new Activity(
                this.id,
                sourceAccountId,
                this.id,
                LocalDateTime.now(),
                money);
        this.activityWindow.addActivity(deposit);
        return true;
    }

    @Value
    public static class AccountId {
        Long value;
    }
}
