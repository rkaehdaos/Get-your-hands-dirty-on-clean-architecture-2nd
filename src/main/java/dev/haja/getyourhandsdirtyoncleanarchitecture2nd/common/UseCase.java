package dev.haja.getyourhandsdirtyoncleanarchitecture2nd.common;

import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 유스케이스 구현(서비스)임을 나타내는 스테레오타입이다.
 * <p>
 * 표지가 아니라 {@link Component}를 메타 애노테이션으로 가지므로, 붙이면 빈으로 등록된다.
 * 포트 인터페이스가 아니라 구현 클래스에 붙인다 — 인터페이스에 붙이면 컴포넌트 스캔이 조용히 건너뛴다.
 */
@Target({TYPE})
@Retention(RUNTIME)
@Documented
@Component
public @interface UseCase {
    @AliasFor(annotation = Component.class)
    String value() default "";
}
