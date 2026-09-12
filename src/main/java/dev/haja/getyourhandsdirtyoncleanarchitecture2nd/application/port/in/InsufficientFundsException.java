package dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.port.in;

import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.model.Account.AccountId;
import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.model.Money;

public class InsufficientFundsException extends RuntimeException {
    public InsufficientFundsException(AccountId sourceAccountId, Money money) {
        super(String.format(
                "Insufficient funds in account %s to transfer %s!",
                sourceAccountId.value(),
                money.amount()));
    }
}
