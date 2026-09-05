package dev.haja.getyourhandsdirtyoncleanarchitecture2nd.buckpal.application.domain.model;

import lombok.*;

import java.time.LocalDateTime;

public class Account {
    private AccountId id;
    private Money baselineBalance;
    private ActivityWindow activityWindow;

    // TODO: 비즈니스 룰 검증: 원본(출금) 계좌의 잔액이 마이너스가 되어서는 안 된다
    public boolean withdraw(Money money, AccountId targetAccountId) {
        if (!mayWithdraw(money))
            return false;
        return true;
    }

    /**
     * 출금 가능 여부를 확인
     */
    private boolean mayWithdraw(Money money) {
        //TODO: 출금 가능 여부를 확인
        return true;
    }
    public record AccountId(Long value) {}
}
