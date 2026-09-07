package dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.port.in;

import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.model.Account.AccountId;
import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.model.Money;
import jakarta.validation.constraints.NotNull;

import static dev.haja.getyourhandsdirtyoncleanarchitecture2nd.common.validation.Validation.validate;

public interface GetAccountBalanceUseCase {

    Money getAccountBalance(GetAccountBalanceQuery query);

    record GetAccountBalanceQuery(
            @NotNull AccountId accountId
    ) {

        public GetAccountBalanceQuery(AccountId accountId) {
            this.accountId = accountId;

            validate(this);
        }
    }
}
