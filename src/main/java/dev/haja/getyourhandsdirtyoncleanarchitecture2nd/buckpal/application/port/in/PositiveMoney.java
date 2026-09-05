package dev.haja.getyourhandsdirtyoncleanarchitecture2nd.buckpal.application.port.in;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({FIELD})
@Retention(RUNTIME)
public @interface PositiveMoney {
}
