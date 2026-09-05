package dev.haja.getyourhandsdirtyoncleanarchitecture2nd.buckpal.application.port.in;

import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.buckpal.application.domain.model.Account;
import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.buckpal.application.domain.model.Money;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.constraints.NotNull;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.assertj.core.api.Assertions.tuple;

class SendMoneyCommandTest {

    private final Account.AccountId sourceAccountId = new Account.AccountId(1L);
    private final Account.AccountId targetAccountId = new Account.AccountId(2L);

    private ConstraintViolationException catchViolation(ThrowableAssert.ThrowingCallable callable) {
        return catchThrowableOfType(ConstraintViolationException.class, callable);
    }

    @Test
    void 유효한_값으로_생성하면_필드가_그대로_담긴다() {
        SendMoneyCommand command = new SendMoneyCommand(sourceAccountId, targetAccountId, Money.of(500L));

        assertThat(command.sourceAccountId()).isEqualTo(sourceAccountId);
        assertThat(command.targetAccountId()).isEqualTo(targetAccountId);
        assertThat(command.money()).isEqualTo(Money.of(500L));
    }

    @Test
    void money가_1이면_경계값으로_통과한다() {
        SendMoneyCommand command = new SendMoneyCommand(sourceAccountId, targetAccountId, Money.of(1L));

        assertThat(command.money()).isEqualTo(Money.of(1L));
    }

    @Test
    void sourceAccountId가_null이면_NotNull_위반이다() {
        ConstraintViolationException ex = catchViolation(() ->
                new SendMoneyCommand(null, targetAccountId, Money.of(500L)));

        assertThat(ex.getConstraintViolations())
                .extracting(v -> v.getPropertyPath().toString(),
                        v -> v.getConstraintDescriptor().getAnnotation().annotationType())
                .containsExactly(tuple("sourceAccountId", NotNull.class));
    }

    @Test
    void targetAccountId가_null이면_NotNull_위반이다() {
        ConstraintViolationException ex = catchViolation(() ->
                new SendMoneyCommand(sourceAccountId, null, Money.of(500L)));

        assertThat(ex.getConstraintViolations())
                .extracting(v -> v.getPropertyPath().toString(),
                        v -> v.getConstraintDescriptor().getAnnotation().annotationType())
                .containsExactly(tuple("targetAccountId", NotNull.class));
    }

    @Test
    void money가_null이면_NotNull_위반만_발생한다() {
        ConstraintViolationException ex = catchViolation(() ->
                new SendMoneyCommand(sourceAccountId, targetAccountId, null));

        assertThat(ex.getConstraintViolations())
                .extracting(v -> v.getPropertyPath().toString(),
                        v -> v.getConstraintDescriptor().getAnnotation().annotationType())
                .containsExactly(tuple("money", NotNull.class));
    }

    @Test
    void money가_0이면_PositiveMoney_위반이다() {
        ConstraintViolationException ex = catchViolation(() ->
                new SendMoneyCommand(sourceAccountId, targetAccountId, Money.ZERO));

        assertThat(ex.getConstraintViolations())
                .extracting(v -> v.getPropertyPath().toString(),
                        v -> v.getConstraintDescriptor().getAnnotation().annotationType())
                .containsExactly(tuple("money", PositiveMoney.class));
        assertThat(ex.getConstraintViolations().iterator().next().getMessage())
                .contains("must be positive");
    }

    @Test
    void money가_음수이면_PositiveMoney_위반이다() {
        ConstraintViolationException ex = catchViolation(() ->
                new SendMoneyCommand(sourceAccountId, targetAccountId, Money.of(-1L)));

        assertThat(ex.getConstraintViolations())
                .extracting(v -> v.getPropertyPath().toString(),
                        v -> v.getConstraintDescriptor().getAnnotation().annotationType())
                .containsExactly(tuple("money", PositiveMoney.class));
        assertThat(ex.getConstraintViolations().iterator().next().getMessage())
                .contains("must be positive");
    }

    @Test
    void 여러_필드가_잘못되면_위반이_모두_수집된다() {
        ConstraintViolationException ex = catchViolation(() ->
                new SendMoneyCommand(null, targetAccountId, Money.of(-1L)));

        assertThat(ex.getConstraintViolations())
                .extracting(v -> v.getPropertyPath().toString(),
                        v -> v.getConstraintDescriptor().getAnnotation().annotationType())
                .containsExactlyInAnyOrder(
                        tuple("sourceAccountId", NotNull.class),
                        tuple("money", PositiveMoney.class));
    }
}
