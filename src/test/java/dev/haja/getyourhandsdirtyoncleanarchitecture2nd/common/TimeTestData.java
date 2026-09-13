package dev.haja.getyourhandsdirtyoncleanarchitecture2nd.common;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * 테스트가 공유하는 고정 시각.
 * <p>
 * {@code NOW}는 마이크로초 정밀도 안에 있는 값이라(나노초 자리가 0) 활동의
 * {@code timestamp(6)} 컬럼을 손실 없이 왕복한다.
 * <p>
 * {@code CLOCK}은 UTC로 고정한 시계다. 시간대가 UTC라
 * {@code LocalDateTime.now(CLOCK)}이 {@code NOW}를 그대로 돌려주므로 오프셋 드리프트가
 * 없고, 서비스가 만든 활동의 시각을 정확히 단언할 수 있다.
 */
public class TimeTestData {

    public static final LocalDateTime NOW = LocalDateTime.of(2026, 9, 12, 12, 0);

    public static final Clock CLOCK = Clock.fixed(NOW.toInstant(ZoneOffset.UTC), ZoneOffset.UTC);
}
