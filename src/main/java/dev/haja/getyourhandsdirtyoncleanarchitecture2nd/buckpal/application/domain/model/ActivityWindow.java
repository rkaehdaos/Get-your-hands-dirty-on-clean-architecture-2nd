package dev.haja.getyourhandsdirtyoncleanarchitecture2nd.buckpal.application.domain.model;

import lombok.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class ActivityWindow {
    private List<Activity> activities;
    public ActivityWindow(@NonNull List<Activity> activities) {
        activities.forEach(activity -> Objects.requireNonNull(
                activity, "activity is marked non-null but is null"));
        this.activities = new ArrayList<>(activities);
    }
    public ActivityWindow(@NonNull Activity... activities) {
        Arrays.asList(activities).forEach(activity -> Objects.requireNonNull(
                activity, "activity is marked non-null but is null"));
        this.activities = new ArrayList<>(Arrays.asList(activities));

    }

    public Money calculateBalance(Account.AccountId accountId) {
        Money depositBalance = activities.stream()
                .filter(activity -> activity.getTargetAccountId().equals(accountId))
                .map(Activity::getMoney)
                .reduce(Money.ZERO, Money::add);
        Money withdrawalBalance = activities.stream()
                .filter(activity -> activity.getSourceAccountId().equals(accountId))
                .map(Activity::getMoney)
                .reduce(Money.ZERO, Money::add);
        return Money.add(depositBalance, withdrawalBalance.negate());
    }

    public void addActivity(@NonNull Activity activity) {
        this.activities.add(activity);
    }
}
