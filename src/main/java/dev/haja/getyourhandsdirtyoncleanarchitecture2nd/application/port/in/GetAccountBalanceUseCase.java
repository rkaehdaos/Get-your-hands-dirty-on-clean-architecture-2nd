package dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.port.in;

import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.model.Account.AccountId;

public interface GetAccountBalanceUseCase {

    record GetAccountBalanceQuery(
            AccountId accountId
    ){}
}
