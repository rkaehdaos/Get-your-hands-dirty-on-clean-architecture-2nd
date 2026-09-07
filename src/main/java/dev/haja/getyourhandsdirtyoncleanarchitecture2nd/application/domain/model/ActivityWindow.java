package dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.model;

import lombok.NonNull;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public record ActivityWindow(
        @NonNull List<Activity> activities) {

    // Compact Constructor를 통한 방어적 복사(Defensive Copy) 및 불변 리스트 처리
    public ActivityWindow {
        activities = List.copyOf(activities);
    }

    // 배열 생성자 지원
    public ActivityWindow(@NonNull Activity... activities) {
        this(Arrays.asList(activities));
    }

    // 가변 addActivity() 대신, 새로운 Record를 반환하도록 변경
    public ActivityWindow addActivity(Activity activity) {
        List<Activity> newActivities = new ArrayList<>(this.activities);
        newActivities.add(activity);
        return new ActivityWindow(newActivities);
    }

    public LocalDateTime getStartTimestamp() {
        return activities.stream()
                .min(Comparator.comparing(Activity::timestamp))
                .orElseThrow(IllegalStateException::new)
                .timestamp();
    }

    public LocalDateTime getEndTimestamp() {
        return activities.stream()
                .max(Comparator.comparing(Activity::timestamp))
                .orElseThrow(IllegalStateException::new)
                .timestamp();
    }
}
