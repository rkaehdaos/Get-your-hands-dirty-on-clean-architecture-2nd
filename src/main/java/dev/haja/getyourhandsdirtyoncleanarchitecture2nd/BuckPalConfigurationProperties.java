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
 * 같은 이유로 {@code buckpal.*} 아래의 <b>모르는 키도 기동을 막는다</b>
 * ({@code ignoreUnknownFields = false}). 기본값을 없애는 것만으로는 기본 yml의 키 누락만
 * 잡힌다 — 기본 yml에 값이 늘 있으므로, 그 값을 덮어쓰려던 프로파일 yml이나 환경변수의
 * 키가 틀리면 덮어쓰기가 조용히 무시되고 기본 yml의 값이 한도로 남는다. 환경변수는
 * {@code BUCKPAL_TRANSFERTHRESHOLD}로 적는다. {@code BUCKPAL_TRANSFER_THRESHOLD}는
 * {@code buckpal.transfer.threshold}로 풀려 모르는 키가 된다.
 * <p>
 * 타입이 {@code long}이 아니라 {@code Long}인 이유는 누락을 {@code @NotNull}로 잡기
 * 위해서다. 원시 타입이면 누락이 0으로 바인딩되어 {@code @Positive} 위반만 남고, 원인이
 * 누락이라는 사실이 가려진다.
 */
@Validated
@ConfigurationProperties(prefix = "buckpal", ignoreUnknownFields = false)
public record BuckPalConfigurationProperties(
        @NotNull @Positive Long transferThreshold) {}
