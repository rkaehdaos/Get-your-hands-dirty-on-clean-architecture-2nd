package dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.service;

import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.model.Money;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MoneyTransferPropertiesTest {

    @Test
    void defaultMaximumTransferThresholdIsOneMillion() {

        // when
        MoneyTransferProperties properties = new MoneyTransferProperties();

        // then
        assertThat(properties.maximumTransferThreshold()).isEqualTo(Money.of(1_000_000L));
    }
}
