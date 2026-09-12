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
        Clock clock = new BuckPalConfiguration().clock();

        // when
        int nanoOfSecond = clock.instant().getNano();

        // then
        assertThat(nanoOfSecond % 1_000).isZero();

        // 시스템 시계의 정밀도가 이미 마이크로초인 플랫폼(현재 macOS가 그렇다)에서는 위
        // 단언이 tick 없이도 통과한다. 어떤 시계인지를 함께 고정한다.
        assertThat(clock).isEqualTo(
                Clock.tick(Clock.systemDefaultZone(), Duration.of(1, ChronoUnit.MICROS)));
    }
}
