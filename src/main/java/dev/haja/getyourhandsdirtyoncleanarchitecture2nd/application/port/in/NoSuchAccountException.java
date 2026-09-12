package dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.port.in;

import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.model.Account.AccountId;

/**
 * 유스케이스가 요구한 계좌가 없어 요청을 수행할 수 없다는 신호.
 * <p>
 * 아웃바운드 포트의 {@code AccountNotFoundException}을 서비스가 이 예외로 번역한다.
 * 인바운드 어댑터는 {@code port.in}의 어휘만 알면 되고, 저장소가 무엇이든 같은 응답을 낸다.
 */
public class NoSuchAccountException extends RuntimeException {

    public NoSuchAccountException(AccountId accountId, Throwable cause) {
        super(String.format("No such account: %s!", accountId.value()), cause);
    }
}
