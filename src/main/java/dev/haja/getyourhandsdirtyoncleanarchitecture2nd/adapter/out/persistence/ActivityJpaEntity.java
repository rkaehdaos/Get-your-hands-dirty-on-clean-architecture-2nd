package dev.haja.getyourhandsdirtyoncleanarchitecture2nd.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 계좌 원장의 한 행. 원장은 append-only다 — 어댑터는 id가 없는 활동만 저장하고
 * 이미 저장된 행은 건드리지 않으므로 모든 컬럼이 {@code updatable = false}다.
 * <p>
 * id를 뺀 나머지는 원시 타입이다. 박싱 타입이면 null이 스키마가 아니라 언박싱
 * 시점의 NPE로 드러나는데, 그 예외에는 어느 컬럼인지가 남지 않는다. id만 {@code Long}인
 * 것은 미영속 활동이 null id로 매핑되고 {@code @GeneratedValue}가 그 null에 의존하기 때문이다.
 * <p>
 * 복합 인덱스는 {@code ActivityRepository}의 세 쿼리가 모두
 * {@code ownerAccountId = ? and timestamp <조건> ?}로 시작하기 때문에 그 순서를 따른다.
 * {@code columnList}에 적은 이름은 물리 컬럼명이 아니라 <b>논리명(프로퍼티명)</b>이다 —
 * 물리 명명 전략이 스네이크 케이스로 바꿔 준다.
 */
@Entity @Data
@Table(name = "activity", indexes = @Index(
        name = "idx_activity_owner_timestamp",
        columnList = "ownerAccountId, timestamp"))
@EqualsAndHashCode(of = {"id"})
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class ActivityJpaEntity {
    @Id @GeneratedValue
    private Long id;

    @Column(nullable = false, updatable = false) private LocalDateTime timestamp;
    @Column(nullable = false, updatable = false) private long ownerAccountId;
    @Column(nullable = false, updatable = false) private long sourceAccountId;
    @Column(nullable = false, updatable = false) private long targetAccountId;
    @Column(nullable = false, updatable = false) private long amount;
}
