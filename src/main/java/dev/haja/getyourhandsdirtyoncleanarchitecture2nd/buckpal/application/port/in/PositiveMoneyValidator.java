package dev.haja.getyourhandsdirtyoncleanarchitecture2nd.buckpal.application.port.in;

import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.buckpal.application.domain.model.Money;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PositiveMoneyValidator implements ConstraintValidator<PositiveMoney, Money> {
    @Override
    public boolean isValid(Money value, ConstraintValidatorContext context) {
        // null일 때 true를 반환해 null 검사는 @NotNull에 위임하도록
        return value == null || value.isPositive();
    }
}
