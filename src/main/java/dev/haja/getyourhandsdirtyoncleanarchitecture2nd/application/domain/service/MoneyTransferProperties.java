package dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.service;

import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.model.Money;

import static java.util.Objects.requireNonNull;
//
/**
 * Configuration properties for money transfer use cases.
 */
public record MoneyTransferProperties(
        Money maximumTransferThreshold) {

    // 기본값을 제공하는 편의용 secondary Constructor
    public MoneyTransferProperties() {
        this(Money.of(1_000_000L));
    }

    // 검증 컴팩트 생성자(Compact Constructor)
    public MoneyTransferProperties {
        requireNonNull(maximumTransferThreshold);
    }

}
