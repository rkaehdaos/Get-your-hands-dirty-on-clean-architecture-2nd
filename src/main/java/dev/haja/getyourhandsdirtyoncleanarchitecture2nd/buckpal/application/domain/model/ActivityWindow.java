package dev.haja.getyourhandsdirtyoncleanarchitecture2nd.buckpal.application.domain.model;

import lombok.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ActivityWindow {
    private List<Activity> activities;
    public ActivityWindow(@NonNull List<Activity> activities) {
        this.activities = activities;
    }
    public ActivityWindow(@NonNull Activity... activities) {
        this.activities = new ArrayList<>(Arrays.asList(activities));
    }
}
