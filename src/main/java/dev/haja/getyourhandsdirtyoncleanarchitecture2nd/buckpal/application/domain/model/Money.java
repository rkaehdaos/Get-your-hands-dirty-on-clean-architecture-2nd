package dev.haja.getyourhandsdirtyoncleanarchitecture2nd.buckpal.application.domain.model;

import java.math.BigInteger;
public record Money(BigInteger amount) {
    public static final Money ZERO = Money.of(0L);

    public static Money of(long longValue) {
        return new Money(BigInteger.valueOf(longValue));
    }

    public static Money add(Money a, Money b) { return new Money(a.amount.add(b.amount)); }
    public static Money subtract(Money a, Money b) { return new Money(a.amount.subtract(b.amount)); }

    public Money negate() { return new Money(this.amount.negate()); }

}
