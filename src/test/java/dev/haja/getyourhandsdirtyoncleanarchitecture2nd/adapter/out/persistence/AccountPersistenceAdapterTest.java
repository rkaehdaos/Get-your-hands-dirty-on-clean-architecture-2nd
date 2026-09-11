package dev.haja.getyourhandsdirtyoncleanarchitecture2nd.adapter.out.persistence;

import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.model.Account;
import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.model.Account.AccountId;

import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.model.Money;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDateTime;

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
        Account account = adapterUnderTest.loadAccount(
                new AccountId(1L),
                LocalDateTime.of(2026, 9, 10, 0, 0));
        assertThat(account.getActivityWindow().activities()).hasSize(2);
        assertThat(account.calculateBalance()).isEqualTo(Money.of(500));

    }
}