package dev.haja.getyourhandsdirtyoncleanarchitecture2nd.adapter.out.persistence;

import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.model.Account.AccountId;
import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.port.out.AccountLock;
import org.springframework.stereotype.Component;

/**
 * 동시성 제어를 도입하기 전까지 쓰는 자리표시자 구현. 아무것도 잠그지 않는다.
 * <p>
 * 실제 잠금으로 교체할 때는 {@link AccountLock}에 적힌 잠금 순서 계약을 지켜야 한다 —
 * 호출자가 전역 일관 순서를 보장하지 않으므로 교착 회피는 구현체의 몫이다.
 */
@Component
class NoOpAccountLock implements AccountLock {

    @Override
    public void lockAccount(AccountId accountId) {
        // do nothing
    }

    @Override
    public void releaseAccount(AccountId accountId) {
        // do nothing
    }
}
