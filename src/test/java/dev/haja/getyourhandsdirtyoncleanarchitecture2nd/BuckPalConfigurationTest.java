package dev.haja.getyourhandsdirtyoncleanarchitecture2nd;

import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.port.in.DistinctAccountsValidator;
import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.port.in.GetAccountBalanceUseCase.GetAccountBalanceQuery;
import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.port.in.PositiveMoneyValidator;
import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.port.in.SendMoneyCommand;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.aot.hint.RuntimeHints;

import java.time.Clock;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.aot.hint.MemberCategory.ACCESS_DECLARED_FIELDS;
import static org.springframework.aot.hint.MemberCategory.INVOKE_DECLARED_CONSTRUCTORS;
import static org.springframework.aot.hint.predicate.RuntimeHintsPredicates.reflection;
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

    @Test
    @DisplayName("스스로 검증하는 입력 모델의 필드와 그 커스텀 검증기의 생성자가 리플렉션 힌트로 등록됨")
    void registersReflectionHintsForSelfValidatingInputModels() {

        // given
        RuntimeHints hints = new RuntimeHints();

        // when
        new BuckPalConfiguration.ValidationRuntimeHints()
                .registerHints(hints, getClass().getClassLoader());

        // then
        // 검증기는 제약 메타데이터에서 뽑힌다. 클래스 레벨 제약(DistinctAccounts)과
        // 프로퍼티 제약(PositiveMoney)이 둘 다 잡히는지 본다
        assertThat(reflection().onType(DistinctAccountsValidator.class)
                .withMemberCategory(INVOKE_DECLARED_CONSTRUCTORS)).accepts(hints);
        assertThat(reflection().onType(PositiveMoneyValidator.class)
                .withMemberCategory(INVOKE_DECLARED_CONSTRUCTORS)).accepts(hints);
        assertThat(reflection().onType(SendMoneyCommand.class)
                .withMemberCategory(ACCESS_DECLARED_FIELDS)).accepts(hints);
        assertThat(reflection().onType(GetAccountBalanceQuery.class)
                .withMemberCategory(ACCESS_DECLARED_FIELDS)).accepts(hints);
    }
}
