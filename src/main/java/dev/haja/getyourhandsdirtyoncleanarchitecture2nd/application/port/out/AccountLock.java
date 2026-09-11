package dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.port.out;

import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.model.Account.AccountId;

/**
 * 계좌 단위 배타 잠금을 표현하는 아웃바운드 포트.
 *
 * <h2>구현체 계약</h2>
 * 호출자는 하나의 트랜잭션 안에서 <b>두 개 이상의 계좌를 순차적으로 잠근다.</b> 잠금 순서는
 * 송금 방향(출금 계좌 → 입금 계좌)을 따르므로, <b>호출자는 전역적으로 일관된 획득 순서를
 * 보장하지 않는다.</b> A→B와 B→A 송금이 동시에 실행되면 두 요청이 서로 반대 순서로 잠금을
 * 요청하게 된다.
 * <p>
 * 따라서 이 포트를 <b>블로킹 잠금으로 구현할 경우, 순서 역전으로 인한 교착은 구현체가
 * 책임진다.</b> 예를 들어 다음 중 하나를 택할 수 있다.
 * <ul>
 *   <li>구현체 내부에서 계좌 ID 같은 전역 순서로 정렬해 잠금을 획득한다</li>
 *   <li>획득에 타임아웃을 두거나 교착을 감지해 요청을 실패로 되돌린다</li>
 *   <li>잠금을 DB 행 잠금에 위임해 교착 감지를 DBMS에 맡긴다</li>
 * </ul>
 * <p>
 * 현재 유일한 구현체는 아무 일도 하지 않는 {@code NoOpAccountLock}이라 대기 자체가 없고,
 * 이 계약은 아직 강제되지 않는다. 실제 잠금 구현을 도입할 때 반드시 다시 검토할 지점이다.
 */
public interface AccountLock {

    /**
     * 계좌를 잠근다. 이미 다른 호출자가 잠그고 있다면 해제될 때까지 기다릴 수 있다.
     *
     * @param accountId 잠글 계좌의 ID
     */
    void lockAccount(AccountId accountId);

    /**
     * 계좌의 잠금을 해제한다.
     * <p>
     * 호출자가 {@code finally} 블록에서 호출하므로 <b>잠금을 보유하지 않은 상태로 호출되더라도
     * 예외를 던지지 않아야 한다.</b>
     *
     * @param accountId 잠금을 해제할 계좌의 ID
     */
    void releaseAccount(AccountId accountId);
}
