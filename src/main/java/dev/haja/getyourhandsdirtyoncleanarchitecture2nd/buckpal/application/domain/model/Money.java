package dev.haja.getyourhandsdirtyoncleanarchitecture2nd.buckpal.application.domain.model;

import lombok.NonNull;
import lombok.Value;

import java.math.BigInteger;
@Value
public class Money {

    public static Money ZERO = Money.of(0L);

    @NonNull BigInteger amount;

    public static Money of(long longValue) {
        return new Money(BigInteger.valueOf(longValue));
    }

    public static Money add(Money a, Money b) { return new Money(a.amount.add(b.amount)); }

    public Money negate() { return new Money(this.amount.negate()); }

}
