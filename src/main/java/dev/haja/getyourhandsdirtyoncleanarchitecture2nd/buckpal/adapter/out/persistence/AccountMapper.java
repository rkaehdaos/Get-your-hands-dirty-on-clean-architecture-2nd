package dev.haja.getyourhandsdirtyoncleanarchitecture2nd.buckpal.adapter.out.persistence;

import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.buckpal.application.domain.model.Account.AccountId;
import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.buckpal.application.domain.model.Activity;
import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.buckpal.application.domain.model.Activity.ActivityId;

import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.buckpal.application.domain.model.ActivityWindow;
import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.buckpal.application.domain.model.Money;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;


@Component
class AccountMapper {
    ActivityWindow mapToActivityWindow(List<ActivityJpaEntity> activities) {
        List<Activity> mappedActivities = new ArrayList<>();

        for (ActivityJpaEntity activity : activities) {
            mappedActivities.add(new Activity(
                    new ActivityId(activity.getId()),
                    new AccountId(activity.getOwnerAccountId()),
                    new AccountId(activity.getSourceAccountId()),
                    new AccountId(activity.getTargetAccountId()),
                    activity.getTimestamp(),
                    Money.of(activity.getAmount())));
        }

        return new ActivityWindow(mappedActivities);
    }
}
