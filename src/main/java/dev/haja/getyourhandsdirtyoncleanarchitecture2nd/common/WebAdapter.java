package dev.haja.getyourhandsdirtyoncleanarchitecture2nd.common;

import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 웹 어댑터 — {@code adapter.in.web}에서 HTTP 요청을 인바운드 포트 호출로, 그 결과와 유스케이스의
 * 예외를 HTTP 응답으로 번역하는 클래스 — 임을 나타내는 스테레오타입이다. 컨트롤러와 예외 핸들러가
 * 여기에 속한다.
 * <p>
 * {@link Component}를 메타 애노테이션으로 가질 뿐 {@code @Controller}도 {@code @ControllerAdvice}도
 * 아니므로, 이것만으로는 요청 핸들러나 예외 핸들러로 인식되지 않는다. 컨트롤러는
 * {@code @RestController}, 예외 핸들러는 {@code @RestControllerAdvice}와 함께 붙인다.
 * 빈 이름은 둘 중 한쪽에만 지정한다 — 서로 다르게 지정하면 기동이 실패한다.
 * {@code @RestControllerAdvice}의 빈 이름 속성은 {@code name}이다. {@code value}는 advice를 적용할
 * 패키지({@code basePackages})라서, 여기에 이름을 주면 advice가 컨트롤러에 적용되지 않는다.
 */
@Target({TYPE})
@Retention(RUNTIME)
@Documented
@Component
public @interface WebAdapter {
    @AliasFor(annotation = Component.class)
    String value() default "";
}
