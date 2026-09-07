package dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.model;

import lombok.NonNull;

import java.util.List;

public record ActivityWindow(
        @NonNull List<Activity> activities) {

    // Compact Constructor를 통한 방어적 복사(Defensive Copy) 및 불변 리스트 처리
    public ActivityWindow {
        activities = List.copyOf(activities);
    }
}
