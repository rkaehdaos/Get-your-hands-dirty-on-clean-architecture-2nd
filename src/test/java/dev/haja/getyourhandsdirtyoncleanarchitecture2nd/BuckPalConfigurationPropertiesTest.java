package dev.haja.getyourhandsdirtyoncleanarchitecture2nd;

import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.model.Money;
import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.service.MoneyTransferProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.validation.BindValidationException;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class BuckPalConfigurationPropertiesTest {

    // application.yml을 읽지 않으므로 설정 누락을 그대로 재현한다
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(BuckPalConfiguration.class);

    @Test
    void bindsTransferThreshold() {

        // given
        ApplicationContextRunner runner = contextRunner
                .withPropertyValues("buckpal.transferThreshold=10000");

        // when
        runner.run(context -> {

            // then
            assertThat(context.getBean(MoneyTransferProperties.class).maximumTransferThreshold())
                    .isEqualTo(Money.of(10_000L));
        });
    }

    @Test
    @DisplayName("송금 한도 설정이 없으면 기동에 실패함")
    void givenNoTransferThreshold_thenFailsToStart() {

        // given
        ApplicationContextRunner runner = contextRunner;

        // when
        runner.run(context -> {

            // then
            assertThat(context).getFailure()
                    .hasRootCauseInstanceOf(BindValidationException.class)
                    .rootCause()
                    .hasMessageContaining("transferThreshold");
        });
    }

    @Test
    @DisplayName("송금 한도가 0이면 기동에 실패함")
    void givenZeroTransferThreshold_thenFailsToStart() {

        // given
        ApplicationContextRunner runner = contextRunner
                .withPropertyValues("buckpal.transferThreshold=0");

        // when
        runner.run(context -> {

            // then
            assertThat(context).getFailure()
                    .hasRootCauseInstanceOf(BindValidationException.class)
                    .rootCause()
                    .hasMessageContaining("transferThreshold");
        });
    }
}
