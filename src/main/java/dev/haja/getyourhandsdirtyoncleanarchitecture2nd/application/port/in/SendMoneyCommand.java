package dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.port.in;

import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.model.Account.AccountId;
import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.model.Money;
import jakarta.validation.constraints.NotNull;

public record SendMoneyCommand(
        @NotNull AccountId sourceAccountId,
        @NotNull AccountId targetAccountId,
        @NotNull Money money) {

    public SendMoneyCommand {
        // TODO: money >0 검증
    }
}
