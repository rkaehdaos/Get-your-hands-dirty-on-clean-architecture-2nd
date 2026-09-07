package dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public record ActivityWindow(List<Activity> activities) {

    // Compact Constructor에서 검증 먼저, 방어적 복사(Defensive Copy)와 불변 리스트화는 그다음
    public ActivityWindow {
        Objects.requireNonNull(activities, "activities must not be null");
        activities = List.copyOf(activities);
    }

    // 배열 생성자 지원
    public ActivityWindow(Activity... activities) {
        // this(...)의 인자식은 정규 생성자보다 먼저 평가된다. 배열이 null이면
        // compact constructor에 닿기 전에 List.of가 메시지 없는 NPE를 던지므로 여기서 직접 검증한다.
        // List.of가 배열을 복사하므로 compact constructor의 List.copyOf는 재복사하지 않는다.
        this(List.of(Objects.requireNonNull(activities, "activities must not be null")));
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
        // null이면 equals가 조용히 false를 반환해 잔액이 ZERO로 나오므로, 여기서 먼저 막는다.
        Objects.requireNonNull(accountId, "accountId must not be null");

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
