package dev.haja.getyourhandsdirtyoncleanarchitecture2nd;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;

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
}
