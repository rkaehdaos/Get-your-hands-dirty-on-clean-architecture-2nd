package dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.model;

import lombok.NonNull;

import java.math.BigInteger;

public record Money(@NonNull BigInteger amount) {
    public static final Money ZERO = Money.of(0L);

    public static Money of(long longValue) {
        return new Money(BigInteger.valueOf(longValue));
    }
}
