package dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.model;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.util.Objects;

@AllArgsConstructor
public class Account {

    private AccountId id;
    private Money baselineBalance;
    private ActivityWindow activityWindow;

    /**
     * 기준 잔액에 거래 내역의 금액을 더하여 계좌의 총 잔액을 계산.
     */
    public Money calculateBalance() {
        return Money.add(
                this.baselineBalance,
                this.activityWindow.calculateBalance(this.id));
    }

    private boolean mayWithdraw(Money money) {
        return Money.add(
                        this.calculateBalance(),
                        money.negate())
                .isPositive();
    }


    /**
     * 식별자가 있는 계좌만 표현한다. "아직 id가 없음"은 AccountId(null)이 아니라
     * AccountId 참조 자체가 null인 것으로만 표현한다.
     * AccountId(null)을 허용하면 서로 다른 미영속 계좌가 equals로 같아진다.
     */
    public record AccountId(Long value) {
        public AccountId {
            Objects.requireNonNull(value, "value must not be null");
        }
    }
}
