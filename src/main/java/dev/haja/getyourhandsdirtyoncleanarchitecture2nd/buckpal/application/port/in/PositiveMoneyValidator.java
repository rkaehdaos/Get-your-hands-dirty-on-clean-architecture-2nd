package dev.haja.getyourhandsdirtyoncleanarchitecture2nd.buckpal.application.port.in;

import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.buckpal.application.domain.model.Money;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.constraints.NotNull;

public class PositiveMoneyValidator implements ConstraintValidator<PositiveMoney, Money> {
    @Override
    public boolean isValid(@NotNull Money value, ConstraintValidatorContext context) {
        return value.isPositive();
    }
}
