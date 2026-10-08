package dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.service;

import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.model.Money;

import static java.util.Objects.requireNonNull;

/**
 * Configuration properties for money transfer use cases.
 */
public record MoneyTransferProperties(
        Money maximumTransferThreshold) {

    // 검증 컴팩트 생성자(Compact Constructor)
    public MoneyTransferProperties {
        requireNonNull(maximumTransferThreshold, "maximumTransferThreshold must not be null");
    }

    // 책의 필드 초기화식(Money.of(1_000_000L))을 옮긴 기본값 생성자.
    // 빈은 BuckPalConfiguration이 buckpal.transferThreshold로 만들므로 이 값을 쓰지 않는다.
    public MoneyTransferProperties() {
        this(Money.of(1_000_000L));
    }

}
