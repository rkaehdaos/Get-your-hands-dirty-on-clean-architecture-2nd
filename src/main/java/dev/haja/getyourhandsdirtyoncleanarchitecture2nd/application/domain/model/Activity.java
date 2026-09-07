package dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.model;

public record Activity(ActivityId id) {
    public record ActivityId(Long value) {}
}

