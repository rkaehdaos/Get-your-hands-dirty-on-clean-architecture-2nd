package dev.haja.getyourhandsdirtyoncleanarchitecture2nd;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * {@code buckpal.*} 설정.
 * <p>
 * 송금 한도에는 <b>기본값이 없다.</b> 설정이 빠지면 기동이 실패한다. relaxed binding은
 * 모르는 키를 무시하므로, 기본값이 있으면 키에 오타가 났을 때 그 기본값이 조용히 쓰인다
 * — 설정을 고쳤다고 믿는 동안 실제로는 코드에 박힌 값이 한도로 남는다. 한도를 두지
 * 않으려면 그 값을 설정에 명시적으로 적는다.
 * <p>
 * 타입이 {@code long}이 아니라 {@code Long}인 이유는 누락을 {@code @NotNull}로 잡기
 * 위해서다. 원시 타입이면 누락이 0으로 바인딩되어 {@code @Positive} 위반만 남고, 원인이
 * 누락이라는 사실이 가려진다.
 */
@Validated
@ConfigurationProperties(prefix = "buckpal")
public record BuckPalConfigurationProperties(
        @NotNull @Positive Long transferThreshold) {}
