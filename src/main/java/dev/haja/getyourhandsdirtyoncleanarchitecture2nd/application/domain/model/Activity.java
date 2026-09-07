package dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.model;

import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.model.Account.AccountId;
import java.time.LocalDateTime;
import java.util.Objects;

public record Activity(
        ActivityId id,
        AccountId ownerAccountId,
        AccountId sourceAccountId,
        AccountId targetAccountId,
        LocalDateTime timestamp,
        Money money) {

    /**
     * id는 아직 영속화되지 않은 신규 활동을 표현하므로 의도적으로 검증하지 않는다(nullable).
     */
    public Activity {
        Objects.requireNonNull(ownerAccountId, "ownerAccountId must not be null");
        Objects.requireNonNull(sourceAccountId, "sourceAccountId must not be null");
        Objects.requireNonNull(targetAccountId, "targetAccountId must not be null");
        Objects.requireNonNull(timestamp, "timestamp must not be null");
        Objects.requireNonNull(money, "money must not be null");
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

    public record ActivityId(Long value) {}

}
