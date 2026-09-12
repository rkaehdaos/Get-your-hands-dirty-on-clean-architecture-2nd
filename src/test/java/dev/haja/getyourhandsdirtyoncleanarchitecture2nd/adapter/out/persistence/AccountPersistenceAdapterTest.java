package dev.haja.getyourhandsdirtyoncleanarchitecture2nd.adapter.out.persistence;

import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.model.Account;
import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.model.Account.AccountId;
import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.model.ActivityWindow;
import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.model.Money;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;

import static dev.haja.getyourhandsdirtyoncleanarchitecture2nd.common.AccountTestData.defaultAccount;
import static dev.haja.getyourhandsdirtyoncleanarchitecture2nd.common.ActivityTestData.defaultActivity;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import({
        AccountPersistenceAdapter.class,
        AccountMapper.class})
class AccountPersistenceAdapterTest {

    @Autowired private AccountPersistenceAdapter adapterUnderTest;
    @Autowired private ActivityRepository activityRepository;
    @Autowired private JdbcTemplate jdbcTemplate;

    @Test
    @Sql("/sql/accounts.sql")
    void loadsAccount() {

        // given
        // @Sql 픽스처가 계좌 1과 그에 속한 활동들을 적재한다

        // when
        Account account = adapterUnderTest.loadAccount(
                new AccountId(1L),
                LocalDateTime.of(2018, 8, 10, 0, 0));

        // then
        // 기준일 이전 활동은 baseline 잔액으로(1000 입금 − 500 출금 = 500),
        // 이후 활동은 윈도우로(1500 입금 − 1000 출금 = 500) 갈라진다
        assertThat(account.getActivityWindow().activities()).hasSize(2);
        assertThat(account.calculateBalance()).isEqualTo(Money.of(1000));
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

    @Test
    @DisplayName("기준 잔액 합계가 long 범위를 넘어도 정확히 조회됨")
    void loadsAccountWhoseBaselineBalanceExceedsLongRange() {

        // given
        // 각각은 long에 담기지만 둘을 더하면 넘치는 입금 두 건.
        // 합계를 long으로 읽으면 이 자리에서 깨진다 — Money는 이미 BigInteger다.
        long almostMax = Long.MAX_VALUE - 1;

        jdbcTemplate.update("insert into account (id) values (1)");
        activityRepository.saveAll(List.of(
                new ActivityJpaEntity(null, LocalDateTime.of(2018, 8, 8, 8, 0), 1L, 2L, 1L, almostMax),
                new ActivityJpaEntity(null, LocalDateTime.of(2018, 8, 9, 8, 0), 1L, 2L, 1L, almostMax)));

        // when
        Account account = adapterUnderTest.loadAccount(
                new AccountId(1L),
                LocalDateTime.of(2018, 8, 10, 0, 0));

        // then
        // 두 활동 모두 기준일 이전이므로 윈도우는 비고 잔액은 전부 baseline에서 온다
        assertThat(account.calculateBalance())
                .isEqualTo(new Money(BigInteger.valueOf(almostMax).multiply(BigInteger.TWO)));
    }

    @Test
    @DisplayName("컬럼이 null인 활동 행은 스키마가 거부함")
    void rejectsNullAmount() {

        // given
        // 스키마의 유일한 출처가 엔티티이므로, 이 테스트가 not null 제약의 존재를 고정한다.
        // 엔티티를 거치지 않고 직접 넣어야 제약이 DB에 있는지를 확인할 수 있다.

        // when / then
        assertThatThrownBy(() -> jdbcTemplate.update("""
                insert into activity
                (id, timestamp, owner_account_id, source_account_id, target_account_id, amount)
                values (1001, '2019-08-09 10:00:00.0', 1, 1, 2, null)
                """))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("owner·timestamp 복합 인덱스가 스키마에 존재함")
    void hasOwnerTimestampIndex() {

        // given
        // @Index의 columnList는 논리명이라 물리 명명 전략을 거쳐야 컬럼명이 맞는데,
        // 이름이 틀려도 DDL 오류는 로그로만 남는다. 인덱스의 존재를 여기서 고정한다.

        // when
        List<String> indexedColumns = jdbcTemplate.queryForList("""
                select column_name from information_schema.index_columns
                where index_name = 'IDX_ACTIVITY_OWNER_TIMESTAMP'
                order by ordinal_position
                """, String.class);

        // then
        assertThat(indexedColumns).containsExactly("OWNER_ACCOUNT_ID", "TIMESTAMP");
    }

}
