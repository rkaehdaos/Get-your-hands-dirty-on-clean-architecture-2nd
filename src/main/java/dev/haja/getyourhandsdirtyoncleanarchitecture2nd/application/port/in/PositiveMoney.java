package dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.port.in;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 유스케이스 입력 모델(커맨드)에만 적용하는 제약이며, {@code Money}의 불변식이 아니다.
 * {@code Money} 자체는 0이나 음수도 표현할 수 있고 그래야 한다(잔액, 출금 등).
 */
@Target({FIELD})
@Retention(RUNTIME)
@Constraint(validatedBy = PositiveMoneyValidator.class)
@Documented
public @interface PositiveMoney {
    String message() default "must be positive" +
            " found: ${validatedValue}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
