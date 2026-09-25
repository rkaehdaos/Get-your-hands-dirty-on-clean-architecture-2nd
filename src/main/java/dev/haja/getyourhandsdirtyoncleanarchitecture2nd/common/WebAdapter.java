package dev.haja.getyourhandsdirtyoncleanarchitecture2nd.common;

import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({TYPE})
@Retention(RUNTIME)
@Documented
@Component
public @interface WebAdapter {
    @AliasFor(annotation = Component.class)
    String value() default "";
}
