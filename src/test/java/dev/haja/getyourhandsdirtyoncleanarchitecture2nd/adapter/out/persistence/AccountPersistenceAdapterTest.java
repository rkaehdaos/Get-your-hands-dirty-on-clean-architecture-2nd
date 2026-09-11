package dev.haja.getyourhandsdirtyoncleanarchitecture2nd.adapter.out.persistence;

import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.model.Account;
import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.model.Account.AccountId;
import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.model.ActivityWindow;
import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.model.Money;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;

import static dev.haja.getyourhandsdirtyoncleanarchitecture2nd.common.AccountTestData.defaultAccount;
import static dev.haja.getyourhandsdirtyoncleanarchitecture2nd.common.ActivityTestData.defaultActivity;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({
        AccountPersistenceAdapter.class,
        AccountMapper.class})
class AccountPersistenceAdapterTest {

    @Autowired private AccountPersistenceAdapter adapterUnderTest;
    @Autowired private ActivityRepository activityRepository;

    @Test
    @Sql("AccountPersistenceAdapterTest.sql")
    void loadsAccount() {

        // given
        // @Sql 픽스처가 계좌 1과 그에 속한 활동들을 적재한다

        // when
        Account account = adapterUnderTest.loadAccount(
                new AccountId(1L),
                LocalDateTime.of(2018, 8, 10, 0, 0));

        // then
        assertThat(account.getActivityWindow().activities()).hasSize(2);
        assertThat(account.calculateBalance()).isEqualTo(Money.of(500));
    }

    @Test
    void updatesActivities() {

        // given
        Account account = defaultAccount()
                .withBaselineBalance(Money.of(555L))
                .withActivityWindow(new ActivityWindow(
                        defaultActivity()
                                .withId(null)
                                .withMoney(Money.of(1L)).build()))
                .build();

        // when
        adapterUnderTest.updateActivities(account);

        // then
        assertThat(activityRepository.count()).isEqualTo(1);

        ActivityJpaEntity savedActivity = activityRepository.findAll().get(0);
        assertThat(savedActivity.getAmount()).isEqualTo(1L);
    }

}
