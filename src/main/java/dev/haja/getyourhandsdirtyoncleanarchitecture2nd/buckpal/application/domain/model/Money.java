package dev.haja.getyourhandsdirtyoncleanarchitecture2nd.buckpal.application.domain.model;

import lombok.NonNull;
import lombok.Value;

import java.math.BigInteger;
@Value
public class Money {
    @NonNull BigInteger amount;
    public static Money add(Money a, Money b) { return new Money(a.amount.add(b.amount)); }
}
