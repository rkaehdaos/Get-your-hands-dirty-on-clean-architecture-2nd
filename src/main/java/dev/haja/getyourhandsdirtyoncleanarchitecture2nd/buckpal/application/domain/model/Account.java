package dev.haja.getyourhandsdirtyoncleanarchitecture2nd.buckpal.application.domain.model;

import lombok.*;

@Getter @ToString
@AllArgsConstructor
public class Account {
    private AccountId id;

    @Value
    public static class AccountId {
        Long value;
    }
}
