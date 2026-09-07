package dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.model;

import lombok.NonNull;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public record ActivityWindow(
        @NonNull List<Activity> activities) {

    // Compact Constructor를 통한 방어적 복사(Defensive Copy) 및 불변 리스트 처리
    public ActivityWindow {
        activities = List.copyOf(activities);
    }

    // 배열 생성자 지원
    public ActivityWindow(Activity... activities) {
        // Lombok @NonNull은 record 정규 생성자에만 체크를 삽입하므로 여기서 직접 검증한다.
        // List.of가 배열을 복사하므로 compact constructor의 List.copyOf는 재복사하지 않는다.
        this(List.of(Objects.requireNonNull(
                activities, "activities is marked non-null but is null")));
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

    /**
     * Calculates the balance by summing up the values of all activities within this window.
     */
    public Money calculateBalance(Account.AccountId accountId) {
        Money depositBalance = activities.stream()
                .filter(a -> a.targetAccountId().equals(accountId))
                .map(Activity::money)
                .reduce(Money.ZERO, Money::add);

        Money withdrawalBalance = activities.stream()
                .filter(a -> a.sourceAccountId().equals(accountId))
                .map(Activity::money)
                .reduce(Money.ZERO, Money::add);

        return Money.add(depositBalance, withdrawalBalance.negate());
    }
}
