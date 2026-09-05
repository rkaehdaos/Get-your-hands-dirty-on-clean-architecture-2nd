package dev.haja.getyourhandsdirtyoncleanarchitecture2nd.buckpal.application.domain.model;

public record Activity(ActivityId id) {
    public record ActivityId(Long value) {}
}

