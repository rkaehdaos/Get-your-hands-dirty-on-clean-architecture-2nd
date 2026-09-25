package dev.haja.getyourhandsdirtyoncleanarchitecture2nd;

import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.port.in.DistinctAccountsValidator;
import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.port.in.GetAccountBalanceUseCase.GetAccountBalanceQuery;
import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.port.in.PositiveMoneyValidator;
import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.port.in.SendMoneyCommand;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.aot.hint.RuntimeHints;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.aot.hint.MemberCategory.ACCESS_DECLARED_FIELDS;
import static org.springframework.aot.hint.MemberCategory.INVOKE_DECLARED_CONSTRUCTORS;
import static org.springframework.aot.hint.predicate.RuntimeHintsPredicates.reflection;

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
