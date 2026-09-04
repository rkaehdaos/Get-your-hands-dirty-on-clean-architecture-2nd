package dev.haja.getyourhandsdirtyoncleanarchitecture2nd.buckpal.application.domain.model;

import lombok.Value;

public class Activity {
    ActivityId id;


    @Value
    public static class ActivityId {
        Long value;
    }
}
