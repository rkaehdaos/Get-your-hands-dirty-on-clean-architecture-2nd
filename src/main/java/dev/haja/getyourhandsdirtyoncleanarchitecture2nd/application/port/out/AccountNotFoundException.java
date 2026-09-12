package dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.port.out;

import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.model.Account.AccountId;

/**
 * 요청한 계좌가 저장소에 없다는 신호.
 * <p>
 * 영속성 기술의 예외(JPA의 {@code EntityNotFoundException} 등)가 포트 경계를 넘지
 * 않도록 어댑터가 이 예외로 바꿔 던진다. 이것은 <b>아웃바운드 포트의 어휘</b>이므로
 * 웹 어댑터까지 그대로 올라가지 않는다 — 서비스가 유스케이스 수준의 거부로 번역한다.
 */
public class AccountNotFoundException extends RuntimeException {

    private final AccountId accountId;

    public AccountNotFoundException(AccountId accountId) {
        super(String.format("Account %s not found!", accountId.value()));
        this.accountId = accountId;
    }

    public AccountId getAccountId() {
        return accountId;
    }
}
