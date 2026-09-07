package dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.port.in;

import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.model.Account.AccountId;
import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.model.Money;

import java.util.Objects;

public record SendMoneyCommand(
        AccountId sourceAccountId,
        AccountId targetAccountId,
        Money money) {

    public SendMoneyCommand {
        Objects.requireNonNull(sourceAccountId);
        Objects.requireNonNull(targetAccountId);
        Objects.requireNonNull(money);
        // TODO: money >0 검증
    }
}
