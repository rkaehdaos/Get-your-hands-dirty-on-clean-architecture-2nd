package dev.haja.getyourhandsdirtyoncleanarchitecture2nd.buckpal.application.domain.service;

import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.buckpal.application.domain.model.Money;
import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.buckpal.application.port.in.GetAccountBalanceUseCase;
import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.buckpal.application.port.out.LoadAccountPort;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@RequiredArgsConstructor
public class GetAccountBalanceService implements GetAccountBalanceUseCase {
    private final LoadAccountPort loadAccountPort;

    @Override
    public Money getAccountBalance(GetAccountBalanceQuery query) {
        return loadAccountPort.loadAccount(query.accountId(), LocalDateTime.now()).calculateBalance();
    }
}
