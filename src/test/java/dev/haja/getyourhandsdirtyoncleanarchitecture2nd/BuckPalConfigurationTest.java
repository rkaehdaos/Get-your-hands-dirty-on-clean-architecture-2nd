package dev.haja.getyourhandsdirtyoncleanarchitecture2nd;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BuckPalConfigurationTest {

    @Test
    void clockTicksInMicroseconds() {

        // given
        BuckPalConfiguration configuration = new BuckPalConfiguration();

        // when
        Clock clock = configuration.clock();

        // then
        // 시계가 내놓은 한 시각의 정밀도가 아니라 어떤 시계인지를 고정한다. 시스템
        // 시계가 이미 마이크로초인 플랫폼(현재 macOS가 그렇다)에서는 tick을 빼도
        // 시각만 보는 단언은 통과해 버린다.
        assertThat(clock).isEqualTo(
                Clock.tick(Clock.systemDefaultZone(), Duration.of(1, ChronoUnit.MICROS)));
    }

    @Test
    @DisplayName("검증을 거치지 않은 송금 한도가 null이면 설정 키를 담은 NPE로 실패함")
    void givenNullTransferThreshold_thenFailsWithSettingKey() {

        // given
        BuckPalConfiguration configuration = new BuckPalConfiguration();
        BuckPalConfigurationProperties properties = new BuckPalConfigurationProperties(null);

        // when / then
        // 컴포넌트명이 아니라 설정 키로 단언한다. 검사가 없어도 언박싱 NPE의 helpful
        // 메시지에 "...BuckPalConfigurationProperties.transferThreshold()"가 들어가,
        // 컴포넌트명만으로는 이 테스트가 통과해 버린다.
        assertThatThrownBy(() -> configuration.moneyTransferProperties(properties))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("buckpal.transferThreshold");
    }
}
