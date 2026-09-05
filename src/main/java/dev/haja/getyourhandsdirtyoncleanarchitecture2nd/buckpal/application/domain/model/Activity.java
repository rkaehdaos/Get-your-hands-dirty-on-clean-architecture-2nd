package dev.haja.getyourhandsdirtyoncleanarchitecture2nd.buckpal.application.domain.model;

import java.time.LocalDateTime;

public record Activity(
        ActivityId id,
        Account.AccountId ownerAccountId,
        Account.AccountId sourceAccountId,
        Account.AccountId targetAccountId,
        LocalDateTime timestamp,
        Money money
        ) {

    public record ActivityId(Long value) {}
}

