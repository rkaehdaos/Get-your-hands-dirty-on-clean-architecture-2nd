package dev.haja.getyourhandsdirtyoncleanarchitecture2nd;

import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.service.MoneyTransferProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

@Configuration
public class BuckPalConfiguration {
    @Bean
    public MoneyTransferProperties moneyTransferProperties(){
        return new MoneyTransferProperties();
    }

    /**
     * 서비스가 현재 시각을 직접 읽지 않고 주입받게 하는 빈. 시간대는
     * {@code LocalDateTime.now()}가 쓰던 기본 시간대 그대로다.
     * <p>
     * 시각을 <b>마이크로초 단위로 끊는다.</b> 활동의 timestamp 컬럼이 {@code timestamp(6)}이라
     * 나노초 자리가 있으면 저장되며 반올림되고(H2는 half-up), 그러면 방금 저장한 활동을 다시
     * 읽었을 때 시각이 달라져 {@code equals}가 깨진다. 정밀도를 저장소에 맞춰 두면 왕복이
     * 손실 없이 끝난다.
     */
    @Bean
    public Clock clock(){
        return Clock.tick(Clock.systemDefaultZone(), Duration.of(1, ChronoUnit.MICROS));
    }
}
