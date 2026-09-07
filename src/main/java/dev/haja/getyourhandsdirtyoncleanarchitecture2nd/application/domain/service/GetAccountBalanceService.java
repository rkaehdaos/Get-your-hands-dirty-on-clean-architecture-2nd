package dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.service;

import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.model.Money;
import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.port.in.GetAccountBalanceUseCase;
import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.port.out.LoadAccountPort;
import lombok.RequiredArgsConstructor;

import java.time.Clock;
import java.time.LocalDateTime;

@RequiredArgsConstructor
class GetAccountBalanceService implements
        GetAccountBalanceUseCase {

    private final LoadAccountPort loadAccountPort;

    /**
     * 현재 시각을 직접 가져오지 않고 Clock에게 물어본다. 시각이 주입 가능한 의존성이 되어
     * 테스트가 baselineDate를 정확히 고정할 수 있다.
     */
    private final Clock clock;

    @Override
    public Money getAccountBalance(GetAccountBalanceQuery query) {
        return loadAccountPort.loadAccount(
                query.accountId(),
                LocalDateTime.now(clock)
        ).calculateBalance();
    }
}
