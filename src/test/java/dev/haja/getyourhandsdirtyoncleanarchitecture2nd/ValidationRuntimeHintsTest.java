package dev.haja.getyourhandsdirtyoncleanarchitecture2nd;

import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.model.Money;
import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.port.in.DistinctAccountsValidator;
import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.port.in.GetAccountBalanceUseCase.GetAccountBalanceQuery;
import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.port.in.PositiveMoneyValidator;
import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.port.in.SendMoneyCommand;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledInNativeImage;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.beans.factory.aot.AotServices;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.aot.hint.MemberCategory.ACCESS_DECLARED_FIELDS;
import static org.springframework.aot.hint.MemberCategory.INVOKE_DECLARED_CONSTRUCTORS;
import static org.springframework.aot.hint.predicate.RuntimeHintsPredicates.reflection;

// 레지스트라는 AOT 빌드 시점에만 쓰인다. 네이티브 이미지 안에서는 클래스 파일을 스캔할 수 없고
// 불러 봐야 확인할 것도 없다.
// 등록된 힌트는 네이티브에서 SendMoneyCommandTest(컨텍스트 없이)와 SendMoneySystemTest(송금
// 경로)가 커맨드를 만들며 쓴다.
@DisabledInNativeImage
class ValidationRuntimeHintsTest {

    @Test
    @DisplayName("스스로 검증하는 입력 모델의 필드와 그 커스텀 검증기의 생성자가 리플렉션 힌트로 등록됨")
    void registersReflectionHintsForSelfValidatingInputModels() {

        // given
        RuntimeHints hints = new RuntimeHints();

        // when
        new BuckPalConfiguration.ValidationRuntimeHints()
                .registerHints(hints, getClass().getClassLoader());

        // then
        // 입력 모델은 port.in 스캔으로 찾는다. GetAccountBalanceQuery는 유스케이스 인터페이스에
        // 중첩된 record라 스캔이 중첩 타입까지 찾는지도 함께 본다. 검증기는 제약 메타데이터에서
        // 뽑힌다. 클래스 레벨 제약(DistinctAccounts)과 프로퍼티 제약(PositiveMoney)이 둘 다
        // 잡히는지 본다
        assertThat(reflection().onType(DistinctAccountsValidator.class)
                .withMemberCategory(INVOKE_DECLARED_CONSTRUCTORS)).accepts(hints);
        assertThat(reflection().onType(PositiveMoneyValidator.class)
                .withMemberCategory(INVOKE_DECLARED_CONSTRUCTORS)).accepts(hints);
        assertThat(reflection().onType(SendMoneyCommand.class)
                .withMemberCategory(ACCESS_DECLARED_FIELDS)).accepts(hints);
        assertThat(reflection().onType(GetAccountBalanceQuery.class)
                .withMemberCategory(ACCESS_DECLARED_FIELDS)).accepts(hints);
    }

    @Test
    void isRegisteredInAotFactories() {

        // when
        List<RuntimeHintsRegistrar> registrars = AotServices.factories(getClass().getClassLoader())
                .load(RuntimeHintsRegistrar.class)
                .asList();

        // then
        // AOT 처리기가 컨텍스트마다 읽는 목록이다. 여기서 빠지면 JVM 테스트는 전부 통과하고
        // 네이티브에서만 HV000064로 드러난다
        assertThat(registrars)
                .hasAtLeastOneElementOfType(BuckPalConfiguration.ValidationRuntimeHints.class);
    }

    @Test
    @DisplayName("캐스케이드(@Valid) 프로퍼티가 있는 입력 모델이면 힌트 등록이 거부됨")
    void givenCascadedProperty_thenRejectsInputModel() {

        // given
        RuntimeHints hints = new RuntimeHints();

        // when / then
        assertThatThrownBy(() -> BuckPalConfiguration.ValidationRuntimeHints
                .registerInputModels(hints, List.of(CascadingInput.class)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("money");
    }

    @Test
    @DisplayName("컨테이너 원소 제약이 있는 입력 모델이면 힌트 등록이 거부됨")
    void givenContainerElementConstraint_thenRejectsInputModel() {

        // given
        RuntimeHints hints = new RuntimeHints();

        // when / then
        assertThatThrownBy(() -> BuckPalConfiguration.ValidationRuntimeHints
                .registerInputModels(hints, List.of(ContainerElementInput.class)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("amounts");
    }

    // 루트 패키지에 있어 레지스트라의 port.in 스캔에는 잡히지 않는다
    record CascadingInput(@Valid Money money) {}

    record ContainerElementInput(List<@NotNull Money> amounts) {}
}
