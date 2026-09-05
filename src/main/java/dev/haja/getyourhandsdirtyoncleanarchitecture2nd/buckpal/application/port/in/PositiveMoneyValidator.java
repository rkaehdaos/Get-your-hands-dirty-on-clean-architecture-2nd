package dev.haja.getyourhandsdirtyoncleanarchitecture2nd.buckpal.application.port.in;

import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.buckpal.application.domain.model.Money;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.math.BigInteger;

public class PositiveMoneyValidator implements ConstraintValidator<PositiveMoney, Money> {
    @Override
    public boolean isValid(Money money, ConstraintValidatorContext context) {
        // null일 때 true를 반환해 null 검사는 @NotNull에 위임하도록
        return money == null || (money.amount().compareTo(BigInteger.ZERO)> 0);
    }
}
