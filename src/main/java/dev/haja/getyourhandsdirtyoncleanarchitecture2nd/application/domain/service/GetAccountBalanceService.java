package dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.service;

import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.model.Account;
import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.model.Account.AccountId;
import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.model.Money;
import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.port.in.GetAccountBalanceUseCase;
import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.port.in.NoSuchAccountException;
import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.port.out.AccountNotFoundException;
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
        return loadAccount(
                query.accountId(),
                LocalDateTime.now(clock)
        ).calculateBalance();
    }

    /**
     * 포트가 던진 예외는 감싸지 않고 그대로 전파하는 것이 기본이지만, "계좌가 없다"는
     * 저장소의 사정이 아니라 유스케이스가 거부됐다는 사실이므로 {@code port.in}의 예외로
     * 번역한다. 인바운드 어댑터가 {@code port.out}을 알지 않아도 된다.
     */
    private Account loadAccount(AccountId accountId, LocalDateTime baselineDate) {
        try {
            return loadAccountPort.loadAccount(accountId, baselineDate);
        } catch (AccountNotFoundException e) {
            throw new NoSuchAccountException(accountId, e);
        }
    }
}
