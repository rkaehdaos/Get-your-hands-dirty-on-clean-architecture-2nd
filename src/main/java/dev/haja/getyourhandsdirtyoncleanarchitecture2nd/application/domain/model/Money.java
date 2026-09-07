package dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.model;

import lombok.NonNull;

import java.math.BigInteger;

public record Money(@NonNull BigInteger amount) {
    public static final Money ZERO = Money.of(0L);

    public static Money of(long longValue) {
        return new Money(BigInteger.valueOf(longValue));
    }

    public static Money add(Money a, Money b) { return new Money(a.amount.add(b.amount)); }
    public static Money subtract(Money a, Money b) { return new Money(a.amount.subtract(b.amount)); }

    public Money plus(Money money) { return new Money(this.amount.add(money.amount));}
    public Money minus(Money money) { return new Money(this.amount.subtract(money.amount));}

}
