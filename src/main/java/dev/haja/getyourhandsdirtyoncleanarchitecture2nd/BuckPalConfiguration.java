package dev.haja.getyourhandsdirtyoncleanarchitecture2nd;

import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.model.Money;
import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.service.MoneyTransferProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

import static java.util.Objects.requireNonNull;

@Configuration
@EnableConfigurationProperties(BuckPalConfigurationProperties.class)
public class BuckPalConfiguration {
    /**
     * 송금 한도를 {@code Money}로 감싼다.
     * <p>
     * 한도가 null이 아니라는 보장은 {@code BuckPalConfigurationProperties}의 {@code @NotNull}이
     * 먼저 맡는다. 여기서 다시 검사하는 것은 그 검증이 돌지 않았을 때를 위해서다 —
     * {@code Money.of(long)}이 {@code Long}을 언박싱하므로, 그대로 두면 어느 설정이 빠졌는지
     * 알 수 없는 NPE가 난다.
     */
    @Bean
    public MoneyTransferProperties moneyTransferProperties(BuckPalConfigurationProperties buckPalConfigurationProperties) {
        Long transferThreshold = requireNonNull(buckPalConfigurationProperties.transferThreshold(),
                "buckpal.transferThreshold must not be null");
        return new MoneyTransferProperties(Money.of(transferThreshold));
    }

    /**
     * 서비스가 현재 시각을 직접 읽지 않고 주입받게 하는 빈. 시간대는
     * {@code LocalDateTime.now()}가 쓰던 기본 시간대 그대로다.
     * <p>
     * 시각을 <b>마이크로초 단위로 끊는다.</b> 활동의 timestamp 컬럼이 {@code timestamp(6)}이라
     * 나노초 자리가 있으면 저장되며 반올림되고(H2는 half-up), 그러면 방금 저장한 활동을 다시
     * 읽었을 때 시각이 달라져 {@code equals}가 깨진다. 정밀도를 저장소에 맞춰 두면 왕복이
     * 손실 없이 끝난다.
     * <p>
     * 반대편 짝은 {@code ActivityJpaEntity}의 {@code secondPrecision = 6}이고,
     * {@code AccountPersistenceAdapterTest.hasMicrosecondTimestampColumn}이 스키마에
     * 남은 정밀도를 고정한다. 다만 그 테스트가 실제로 고정하는 것은 <b>컬럼 정밀도가
     * 여기의 tick(1μs)을 담을 수 있다</b>는 것까지다 — H2 방언 기본값이 이미
     * {@code timestamp(6)}이라 엔티티의 애노테이션을 지워도 그 단언은 통과한다.
     * 애노테이션은 정밀도를 방언에 맡기지 않겠다는 명시이고, 그 값어치는 방언이
     * 바뀌는 날 드러난다. 여기의 tick과 그쪽의 정밀도는 함께 움직여야 한다.
     */
    @Bean
    public Clock clock(){
        return Clock.tick(Clock.systemDefaultZone(), Duration.of(1, ChronoUnit.MICROS));
    }
}
