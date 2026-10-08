package dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.service;

import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.model.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MoneyTransferPropertiesTest {

    @Test
    void defaultMaximumTransferThresholdIsOneMillion() {

        // when
        MoneyTransferProperties properties = new MoneyTransferProperties();

        // then
        assertThat(properties.maximumTransferThreshold()).isEqualTo(Money.of(1_000_000L));
    }

    @Test
    @DisplayName("송금 한도가 null이면 생성 시점에 NPE로 실패함")
    void givenNullThreshold_thenFails() {

        // when / then
        assertThatThrownBy(() -> new MoneyTransferProperties(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("maximumTransferThreshold");
    }
}
