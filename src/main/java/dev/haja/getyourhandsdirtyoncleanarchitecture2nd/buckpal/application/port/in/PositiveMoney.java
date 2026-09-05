package dev.haja.getyourhandsdirtyoncleanarchitecture2nd.buckpal.application.port.in;

import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;

@Target({FIELD})
public @interface PositiveMoney {
}
