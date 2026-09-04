package dev.haja.getyourhandsdirtyoncleanarchitecture2nd.buckpal.application.domain.model;

import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import lombok.Value;
@RequiredArgsConstructor
public class Activity {
    ActivityId id;
    @Nonnull Account.AccountId ownerAccountId;

    @Value
    public static class ActivityId {
        Long value;
    }
}
