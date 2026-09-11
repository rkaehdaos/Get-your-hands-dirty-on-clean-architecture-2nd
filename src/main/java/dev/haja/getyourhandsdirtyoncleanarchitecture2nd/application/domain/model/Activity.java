package dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.model;

import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.model.Account.AccountId;

import java.time.LocalDateTime;

import static java.util.Objects.requireNonNull;

/**
 * 계좌 간 자금 이동 한 건을 그 계좌의 원장(ledger)에 기록한 행.
 * <p>
 * ownerAccountId는 이 행이 어느 계좌의 원장에 속하는지를 나타내는 영속성 키다. 송금 하나는
 * 두 행으로 기록된다 — source 계좌 원장에 owner가 source인 행, target 계좌 원장에 owner가
 * target인 행. 조회도 owner 기준이다(ActivityRepository.findByOwnerSince).
 * <p>
 * <b>도메인 계산은 owner를 읽지 않는다.</b> ActivityWindow.calculateBalance는 source와
 * target만 보고, Account.withdraw/deposit은 owner에 자기 id를 쓰기만 한다. 그래서
 * "owner는 source 또는 target 중 하나여야 한다"는 규칙을 여기서 강제하지 않는다. 실제 규칙은
 * 위의 두 행이 짝을 이뤄야 한다는 행 간(cross-row) 규칙이라 Activity 하나만 보고는 표현할 수
 * 없고(출금 행에 owner를 target으로 잘못 달아도 그 검사는 통과한다), 강제할 자리는 도메인이
 * 아니라 영속성이다 — DB CHECK 제약이나 어댑터 테스트.
 */
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
     */
    public Activity {
        requireNonNull(ownerAccountId, "ownerAccountId must not be null");
        requireNonNull(sourceAccountId, "sourceAccountId must not be null");
        requireNonNull(targetAccountId, "targetAccountId must not be null");
        requireNonNull(timestamp, "timestamp must not be null");
        requireNonNull(money, "money must not be null");
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
