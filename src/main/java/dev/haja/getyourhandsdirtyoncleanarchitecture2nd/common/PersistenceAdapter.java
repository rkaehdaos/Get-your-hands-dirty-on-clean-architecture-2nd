package dev.haja.getyourhandsdirtyoncleanarchitecture2nd.common;

import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 영속성 어댑터 — {@code adapter.out.persistence}에서 아웃바운드 포트를 구현하는 클래스 — 임을
 * 나타내는 스테레오타입이다. 이 패키지에 있다면 저장소에 접근하지 않는 구현체(잠금 자리표시자 등)도
포함한다.
 * <p>
 * 표지가 아니라 {@link Component}를 메타 애노테이션으로 가지므로, 붙이면 빈으로 등록된다.
 */
@Target({TYPE})
@Retention(RUNTIME)
@Documented
@Component
public @interface PersistenceAdapter {
    @AliasFor(annotation = Component.class)
    String value() default "";
}
