package dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.port.in;

import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.model.Account.AccountId;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class DistinctAccountsValidator implements
        ConstraintValidator<DistinctAccounts, SendMoneyCommand> {

    /**
     * 두 ID 중 하나라도 null이면 유효한 것으로 본다. Bean Validation 규약상 null 여부는
     * 컴포넌트의 {@code @NotNull}이 책임진다 — 클래스 레벨 제약과 같은 그룹에서 함께
     * 평가되므로 여기서 null을 다시 판단하면 위반이 두 번 보고된다.
     */
    @Override
    public boolean isValid(SendMoneyCommand command, ConstraintValidatorContext context) {
        AccountId sourceAccountId = command.sourceAccountId();
        AccountId targetAccountId = command.targetAccountId();

        if (sourceAccountId == null || targetAccountId == null) return true;
        if (!sourceAccountId.equals(targetAccountId)) return true;

        // 클래스 레벨 제약은 프로퍼티 경로가 비어 있어 위반 메시지가 ": ..."로 시작한다.
        // 위반을 targetAccountId에 붙여 다른 제약들과 같은 "<컴포넌트명>: <메시지>" 꼴을
        // 유지한다 — ProblemDetail의 detail이 그 메시지를 그대로 쓴다.
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                .addPropertyNode("targetAccountId")
                .addConstraintViolation();
        return false;
    }
}
