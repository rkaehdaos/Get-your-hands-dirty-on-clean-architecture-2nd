package dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.model;

import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.model.Account.AccountId;

import java.time.LocalDateTime;

import static java.util.Objects.requireNonNull;

public record Activity(
        ActivityId id,
        AccountId ownerAccountId,
        AccountId sourceAccountId,
        AccountId targetAccountId,
        LocalDateTime timestamp,
        Money money) {

    /**
     * id는 아직 영속화되지 않은 신규 활동을 표현하므로 의도적으로 검증하지 않는다(nullable).
     * 단, "id 없음"은 id 참조가 null인 것으로만 표현한다 — ActivityId(null)은 허용하지 않는다.
     * <p>
     * ownerAccountId는 이 활동을 자기 윈도우에 담는 계좌다. 따라서 반드시 source 또는 target
     * 이어야 한다. 어느 쪽도 아닌 행은 그 계좌의 잔액에 아무 영향을 주지 못하는 무의미한 행이다.
     */
    public Activity {
        requireNonNull(ownerAccountId, "ownerAccountId must not be null");
        requireNonNull(sourceAccountId, "sourceAccountId must not be null");
        requireNonNull(targetAccountId, "targetAccountId must not be null");
        requireNonNull(timestamp, "timestamp must not be null");
        requireNonNull(money, "money must not be null");

        if (!ownerAccountId.equals(sourceAccountId) && !ownerAccountId.equals(targetAccountId)) {
            throw new IllegalArgumentException(
                    "ownerAccountId must be either sourceAccountId or targetAccountId");
        }
    }

    /**
     * id 없이 객체를 생성할 때 사용하는 생성자 (id는 null로 초기화)
     */
    public Activity(
            Account.AccountId ownerAccountId,
            Account.AccountId sourceAccountId,
            Account.AccountId targetAccountId,
            LocalDateTime timestamp,
            Money money
    ) {
        this(null, ownerAccountId, sourceAccountId, targetAccountId, timestamp, money);
    }

    public record ActivityId(Long value) {
        public ActivityId {
            requireNonNull(value, "value must not be null");
        }
    }

}
