package dev.haja.getyourhandsdirtyoncleanarchitecture2nd.adapter.out.persistence;

import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.model.Activity;
import org.springframework.stereotype.Component;

@Component
class AccountMapper {

    ActivityJpaEntity mapToJpaEntity(Activity activity) {
        return new ActivityJpaEntity(
                activity.id() == null ? null : activity.id().value(),
                activity.timestamp(),
                activity.ownerAccountId().value(),
                activity.sourceAccountId().value(),
                activity.targetAccountId().value(),
                activity.money().amount().longValue());
    }
}
