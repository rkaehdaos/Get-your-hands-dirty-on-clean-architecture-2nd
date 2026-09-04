package dev.haja.getyourhandsdirtyoncleanarchitecture2nd.buckpal.application.domain.model;

import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import lombok.Value;

import java.time.LocalDateTime;

@RequiredArgsConstructor
public class Activity {
    ActivityId id;
    @Nonnull Account.AccountId ownerAccountId;
    @Nonnull Account.AccountId sourceAccountId;
    @Nonnull Account.AccountId targetAccountId;
    @Nonnull LocalDateTime timestamp;
    @Nonnull Money money;


    @Value
    public static class ActivityId {
        Long value;
    }
}
