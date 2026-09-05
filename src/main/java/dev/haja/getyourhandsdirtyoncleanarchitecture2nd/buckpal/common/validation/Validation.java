package dev.haja.getyourhandsdirtyoncleanarchitecture2nd.buckpal.common.validation;

import jakarta.validation.Validator;

import static jakarta.validation.Validation.buildDefaultValidatorFactory;

public class Validation {
    private final static Validator validator =
            buildDefaultValidatorFactory().getValidator();
}
