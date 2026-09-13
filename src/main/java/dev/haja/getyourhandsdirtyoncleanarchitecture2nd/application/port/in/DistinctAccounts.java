package dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.port.in;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 출금 계좌와 입금 계좌가 서로 달라야 한다는, 유스케이스 입력 모델(커맨드)의 제약이다.
 * <p>
 * 자기 이체는 순효과가 0인데도 원장에 활동 두 건을 남기고, 같은 계좌를 두 번 잠근다.
 * 커맨드의 두 필드만으로 판정되므로 서비스가 아니라 여기서 막는다.
 */
@Target({TYPE})
@Retention(RUNTIME)
@Constraint(validatedBy = DistinctAccountsValidator.class)
@Documented
public @interface DistinctAccounts {
    String message() default "must differ from sourceAccountId";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
