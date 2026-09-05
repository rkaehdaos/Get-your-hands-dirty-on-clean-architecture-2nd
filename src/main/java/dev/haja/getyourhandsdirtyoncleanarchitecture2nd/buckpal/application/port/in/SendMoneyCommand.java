package dev.haja.getyourhandsdirtyoncleanarchitecture2nd.buckpal.application.port.in;

import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.buckpal.application.domain.model.Money;

import static dev.haja.getyourhandsdirtyoncleanarchitecture2nd.buckpal.application.domain.model.Account.AccountId;

public record SendMoneyCommand(
        AccountId sourceAccountId,
        AccountId targetAccountId,
        Money money
        ) {
}
