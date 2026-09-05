package dev.haja.getyourhandsdirtyoncleanarchitecture2nd.buckpal.application.domain.model;

import java.math.BigInteger;
public record Money(BigInteger amount) {
    public static Money of(long longValue) {
        return new Money(BigInteger.valueOf(longValue));
    }
}
