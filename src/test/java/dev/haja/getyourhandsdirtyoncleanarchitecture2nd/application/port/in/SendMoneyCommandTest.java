package dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.port.in;

import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.model.Money;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static dev.haja.getyourhandsdirtyoncleanarchitecture2nd.common.AccountTestData.DEFAULT_ACCOUNT_ID;
import static dev.haja.getyourhandsdirtyoncleanarchitecture2nd.common.AccountTestData.OTHER_ACCOUNT_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SendMoneyCommandTest {

    @Test
    @DisplayName("출금 계좌와 입금 계좌가 같으면 커맨드 생성이 거부됨")
    void givenSameSourceAndTargetAccount_thenThrowsConstraintViolationException() {

        // when / then
        assertThatThrownBy(() -> new SendMoneyCommand(
                DEFAULT_ACCOUNT_ID,
                DEFAULT_ACCOUNT_ID,
                Money.of(500L)))
                .isInstanceOf(ConstraintViolationException.class)
                .hasMessageContaining("targetAccountId");
    }

    @Test
    @DisplayName("출금 계좌와 입금 계좌가 다르면 커맨드가 정상 생성됨")
    void givenDistinctSourceAndTargetAccount_thenDoesNotThrow() {

        // when / then
        assertThatCode(() -> new SendMoneyCommand(
                OTHER_ACCOUNT_ID,
                DEFAULT_ACCOUNT_ID,
                Money.of(500L)))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("송금액이 양수가 아니면 커맨드 생성이 거부됨")
    void givenNonPositiveMoney_thenThrowsConstraintViolationException() {

        // given
        Money zero = Money.of(0L);

        // when / then
        // 메시지의 ${validatedValue}가 보간됐는지도 본다. 네이티브에서 EL 평가가 실패하면
        // Hibernate Validator는 경고만 남기고 템플릿을 그대로 두는데, 프로퍼티 경로(money)는
        // 그래도 메시지에 들어가 첫 단언만으로는 통과해 버린다
        assertThatThrownBy(() -> new SendMoneyCommand(
                OTHER_ACCOUNT_ID,
                DEFAULT_ACCOUNT_ID,
                zero))
                .isInstanceOf(ConstraintViolationException.class)
                .hasMessageContaining("money")
                .hasMessageContaining(zero.toString());
    }

    // null 컴포넌트는 @NotNull만 위반으로 보고한다. 커스텀 검증기(@DistinctAccounts,
    // @PositiveMoney)가 null을 위반으로 보면 위반이 하나 더 붙고(@DistinctAccounts는 늘
    // targetAccountId에 붙인다), null을 거르지 않으면 NPE가 ValidationException으로 올라온다

    @Test
    @DisplayName("출금 계좌가 null이면 sourceAccountId 위반 하나만 보고됨")
    void givenNullSourceAccount_thenReportsSingleViolation() {

        // when / then
        assertThatThrownBy(() -> new SendMoneyCommand(
                null,
                DEFAULT_ACCOUNT_ID,
                Money.of(500L)))
                .isInstanceOfSatisfying(ConstraintViolationException.class, e ->
                        assertThat(e.getConstraintViolations())
                                .singleElement()
                                .extracting(v -> v.getPropertyPath().toString())
                                .isEqualTo("sourceAccountId"));
    }

    @Test
    @DisplayName("입금 계좌가 null이면 targetAccountId 위반 하나만 보고됨")
    void givenNullTargetAccount_thenReportsSingleViolation() {

        // when / then
        assertThatThrownBy(() -> new SendMoneyCommand(
                OTHER_ACCOUNT_ID,
                null,
                Money.of(500L)))
                .isInstanceOfSatisfying(ConstraintViolationException.class, e ->
                        assertThat(e.getConstraintViolations())
                                .singleElement()
                                .extracting(v -> v.getPropertyPath().toString())
                                .isEqualTo("targetAccountId"));
    }

    @Test
    @DisplayName("송금액이 null이면 money 위반 하나만 보고됨")
    void givenNullMoney_thenReportsSingleViolation() {

        // when / then
        assertThatThrownBy(() -> new SendMoneyCommand(
                OTHER_ACCOUNT_ID,
                DEFAULT_ACCOUNT_ID,
                null))
                .isInstanceOfSatisfying(ConstraintViolationException.class, e ->
                        assertThat(e.getConstraintViolations())
                                .singleElement()
                                .extracting(v -> v.getPropertyPath().toString())
                                .isEqualTo("money"));
    }
}
