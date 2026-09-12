package dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.port.out;

import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.model.Account;

public interface UpdateAccountStatePort {

    /**
     * 계좌의 활동 중 <b>아직 저장되지 않은 것(id가 없는 것)만</b> 저장한다. 이미 id가 있는
     * 활동은 건드리지 않는다.
     * <p>
     * 저장하며 생성된 id는 <b>도메인 객체에 되돌리지 않는다.</b> 넘긴 계좌의 활동은 호출
     * 뒤에도 여전히 id가 없으므로, <b>같은 {@link Account} 인스턴스로 두 번 호출하면 같은
     * 활동이 두 번 저장된다.</b> 호출자는 인스턴스당 한 번만 호출해야 한다.
     *
     * @param account 저장할 활동을 담은 계좌
     * @throws IllegalStateException    계좌에 id가 없으면 — 활동의 소유자를 견줄 대상이 없다
     * @throws IllegalArgumentException 활동의 소유자가 계좌 id와 다르면
     */
    void updateActivities(Account account);
}
