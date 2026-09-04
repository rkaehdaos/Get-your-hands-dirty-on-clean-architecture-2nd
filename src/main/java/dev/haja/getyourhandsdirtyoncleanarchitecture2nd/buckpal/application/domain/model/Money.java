package dev.haja.getyourhandsdirtyoncleanarchitecture2nd.buckpal.application.domain.model;

import lombok.NonNull;
import lombok.Value;

import java.math.BigInteger;
@Value
public class Money {
    @NonNull BigInteger amount;
}
