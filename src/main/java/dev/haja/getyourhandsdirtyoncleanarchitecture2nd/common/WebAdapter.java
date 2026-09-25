package dev.haja.getyourhandsdirtyoncleanarchitecture2nd.common;

import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 웹 어댑터임을 나타내는 스테레오타입이다.
 * <p>
 * {@link Component}를 메타 애노테이션으로 가질 뿐 {@code @Controller}가 아니므로, 이것만으로는
 * 요청 핸들러로 인식되지 않는다. {@code @RestController}와 함께 붙인다.
 * 빈 이름은 둘 중 한쪽에만 지정한다 — 서로 다르게 지정하면 기동이 실패한다.
 */
@Target({TYPE})
@Retention(RUNTIME)
@Documented
@Component
public @interface WebAdapter {
    @AliasFor(annotation = Component.class)
    String value() default "";
}
