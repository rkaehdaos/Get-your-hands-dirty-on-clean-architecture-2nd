package dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.model;

import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.model.Account.AccountId;

import java.time.LocalDateTime;

public record Activity(
        ActivityId id,
        AccountId ownerAccountId,
        AccountId sourceAccountId,
        AccountId targetAccountId,
        LocalDateTime timestamp,
        Money money) {

    public record ActivityId(Long value) {}

}
