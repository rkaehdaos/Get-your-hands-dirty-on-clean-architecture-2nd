package dev.haja.getyourhandsdirtyoncleanarchitecture2nd.adapter.out.persistence;

import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.model.Activity;
import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.model.Activity.ActivityId;
import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.model.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static dev.haja.getyourhandsdirtyoncleanarchitecture2nd.common.AccountTestData.DEFAULT_ACCOUNT_ID;
import static dev.haja.getyourhandsdirtyoncleanarchitecture2nd.common.AccountTestData.OTHER_ACCOUNT_ID;
import static dev.haja.getyourhandsdirtyoncleanarchitecture2nd.common.ActivityTestData.defaultActivity;
import static dev.haja.getyourhandsdirtyoncleanarchitecture2nd.common.TimeTestData.NOW;
import static org.assertj.core.api.Assertions.assertThat;

class AccountMapperTest {

    private final AccountMapper mapper = new AccountMapper();

    @Test
    @DisplayName("활동의 값이 엔티티의 컬럼으로 풀려 옮겨짐")
    void mapsActivityToJpaEntity() {

        // given
        Activity activity = defaultActivity()
                .withMoney(Money.of(500L))
                .build();

        // when
        ActivityJpaEntity entity = mapper.mapToJpaEntity(activity);

        // then
        assertThat(entity.getId()).isNull();
        assertThat(entity.getTimestamp()).isEqualTo(NOW);
        assertThat(entity.getOwnerAccountId()).isEqualTo(DEFAULT_ACCOUNT_ID.value());
        assertThat(entity.getSourceAccountId()).isEqualTo(DEFAULT_ACCOUNT_ID.value());
        assertThat(entity.getTargetAccountId()).isEqualTo(OTHER_ACCOUNT_ID.value());
        assertThat(entity.getAmount()).isEqualTo(500L);
    }

    @Test
    @DisplayName("id가 있는 활동은 그 id가 엔티티로 옮겨짐")
    void mapsActivityIdToJpaEntity() {

        // given
        // 어댑터는 id 없는 활동만 매핑하므로 이 경로는 어댑터 테스트로는 닿지 않는다
        Activity activity = defaultActivity()
                .withId(new ActivityId(1001L))
                .build();

        // when
        ActivityJpaEntity entity = mapper.mapToJpaEntity(activity);

        // then
        assertThat(entity.getId()).isEqualTo(1001L);
    }
}
