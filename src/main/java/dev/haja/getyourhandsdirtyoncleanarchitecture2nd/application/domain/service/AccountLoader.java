package dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.service;

import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.model.Account;
import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.model.Account.AccountId;
import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.port.in.NoSuchAccountException;
import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.port.out.AccountNotFoundException;
import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.port.out.LoadAccountPort;

import java.time.LocalDateTime;

/**
 * 계좌를 불러오며 아웃바운드 포트의 예외를 유스케이스의 어휘로 번역하는 헬퍼.
 * <p>
 * 포트가 던진 예외는 감싸지 않고 그대로 전파하는 것이 기본이지만, "계좌가 없다"는
 * 저장소의 사정이 아니라 유스케이스가 거부됐다는 사실이므로 {@code port.in}의 예외로
 * 번역한다. 인바운드 어댑터가 {@code port.out}을 알지 않아도 된다.
 * <p>
 * 이 번역을 {@code LoadAccountPort}의 default 메서드로 두지 않는 이유는 방향이다 —
 * {@code port.out}이 {@code port.in}을 알게 된다. 서비스가 공유하는 구현 세부라
 * 서비스 패키지에 package-private으로 둔다.
 */
final class AccountLoader {

    private AccountLoader() {
    }

    static Account loadAccount(
            LoadAccountPort loadAccountPort,
            AccountId accountId,
            LocalDateTime baselineDate) {

        try {
            return loadAccountPort.loadAccount(accountId, baselineDate);
        } catch (AccountNotFoundException e) {
            throw new NoSuchAccountException(accountId, e);
        }
    }
}
