package dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.port.in;

import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.model.Money;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PositiveMoneyValidator implements
        ConstraintValidator<PositiveMoney, Money> {

    /**
     * null은 유효한 것으로 본다. Bean Validation 규약상 null 여부는
     * {@code @NotNull}이 책임지므로, 검증기는 null을 판단하지 않는다.
     */
    @Override
    public boolean isValid(Money value, ConstraintValidatorContext context) {
        return value == null || value.isPositive();
    }
}
