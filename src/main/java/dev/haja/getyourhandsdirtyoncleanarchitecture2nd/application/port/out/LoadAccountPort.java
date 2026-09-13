package dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.port.out;

import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.model.Account;
import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.model.Account.AccountId;

import java.time.LocalDateTime;

public interface LoadAccountPort {

    /**
     * 계좌를 불러온다. 활동은 {@code baselineDate} 이후의 것만 담기고, 그 이전 활동은
     * 기준 잔액으로 합산된다.
     *
     * @throws AccountNotFoundException 해당 ID의 계좌가 없으면
     */
    Account loadAccount(AccountId accountId, LocalDateTime baselineDate);

}
