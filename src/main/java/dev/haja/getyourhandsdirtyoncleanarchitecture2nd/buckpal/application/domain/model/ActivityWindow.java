package dev.haja.getyourhandsdirtyoncleanarchitecture2nd.buckpal.application.domain.model;

import lombok.NonNull;

import java.util.List;

public record ActivityWindow(
        @NonNull List<Activity> activities) {}
