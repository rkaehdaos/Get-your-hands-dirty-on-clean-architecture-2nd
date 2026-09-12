package dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.port.in;

import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.model.Account.AccountId;
import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.model.Money;
import jakarta.validation.constraints.NotNull;

import static dev.haja.getyourhandsdirtyoncleanarchitecture2nd.common.validation.Validation.validate;

public interface GetAccountBalanceUseCase {

    /**
     * @throws NoSuchAccountException 조회할 계좌가 없으면
     */
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
