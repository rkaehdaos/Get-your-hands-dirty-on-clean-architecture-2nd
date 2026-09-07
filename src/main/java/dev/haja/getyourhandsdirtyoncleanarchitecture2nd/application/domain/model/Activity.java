package dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.model;

import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.model.Account.AccountId;
import lombok.NonNull;

import java.time.LocalDateTime;

public record Activity(
        ActivityId id,
        @NonNull AccountId ownerAccountId,
        @NonNull AccountId sourceAccountId,
        @NonNull AccountId targetAccountId,
        @NonNull LocalDateTime timestamp,
        @NonNull Money money) {

    /**
     * id 없이 객체를 생성할 때 사용하는 생성자 (id는 null로 초기화)
     */
    public Activity(
            Account.AccountId ownerAccountId,
            Account.AccountId sourceAccountId,
            Account.AccountId targetAccountId,
            LocalDateTime timestamp,
            Money money
    ) {
        this(null, ownerAccountId, sourceAccountId, targetAccountId, timestamp, money);
    }

    public record ActivityId(Long value) {}

}
