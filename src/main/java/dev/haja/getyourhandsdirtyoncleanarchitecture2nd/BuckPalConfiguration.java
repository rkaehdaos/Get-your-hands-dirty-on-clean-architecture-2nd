package dev.haja.getyourhandsdirtyoncleanarchitecture2nd;

import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.service.MoneyTransferProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class BuckPalConfiguration {
    @Bean
    public MoneyTransferProperties moneyTransferProperties(){
        return new MoneyTransferProperties();
    }

    /**
     * 서비스가 현재 시각을 직접 읽지 않고 주입받게 하는 빈.
     * {@code LocalDateTime.now()}가 쓰던 기본 시간대와 같아 동작이 바뀌지 않는다.
     */
    @Bean
    public Clock clock(){
        return Clock.systemDefaultZone();
    }
}
