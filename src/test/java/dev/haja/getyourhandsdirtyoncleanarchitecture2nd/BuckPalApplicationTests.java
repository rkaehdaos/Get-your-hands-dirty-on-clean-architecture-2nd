package dev.haja.getyourhandsdirtyoncleanarchitecture2nd;

import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.model.Money;
import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.service.MoneyTransferProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class BuckPalApplicationTests {

    @Autowired
    private MoneyTransferProperties moneyTransferProperties;

    @Test void contextLoads() {}

    @Test
    @DisplayName("송금 한도는 application.yml이 정한 Long.MAX_VALUE임")
    void transferThresholdIsUnlimited() {

        // when
        Money threshold = moneyTransferProperties.maximumTransferThreshold();

        // then
        assertThat(threshold).isEqualTo(Money.of(Long.MAX_VALUE));
    }

}
