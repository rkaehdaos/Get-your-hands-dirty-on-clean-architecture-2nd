package dev.haja.getyourhandsdirtyoncleanarchitecture2nd.adapter.out.persistence;

import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.model.Account;
import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.model.Account.AccountId;
import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.model.Activity;
import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.model.Activity.ActivityId;
import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.model.ActivityWindow;
import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.model.Money;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import({AccountPersistenceAdapter.class, AccountMapper.class})
class AccountPersistenceAdapterTest {

    private static final AccountId 없는_계좌 = new AccountId(999L);
    private static final LocalDateTime 기준시각 = LocalDateTime.of(2026, 1, 1, 0, 0);

    @Autowired
    private AccountPersistenceAdapter adapter;

    @Autowired
    private SpringDataAccountRepository accountRepository;

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private TestEntityManager entityManager;

    private AccountId 계좌_A;
    private AccountId 계좌_B;

    @BeforeEach
    void setUp() {
        계좌_A = 저장된계좌();
        계좌_B = 저장된계좌();
    }

    private AccountId 저장된계좌() {
        return new AccountId(accountRepository.saveAndFlush(new AccountJpaEntity(null)).getId());
    }

    private ActivityJpaEntity 입금행(AccountId owner, AccountId source,
                                  LocalDateTime timestamp, long amount) {
        return new ActivityJpaEntity(
                null, timestamp, owner.value(), source.value(), owner.value(), amount);
    }

    private ActivityJpaEntity 출금행(AccountId owner, AccountId target,
                                  LocalDateTime timestamp, long amount) {
        return new ActivityJpaEntity(
                null, timestamp, owner.value(), owner.value(), target.value(), amount);
    }

    private ActivityJpaEntity 저장된행(ActivityJpaEntity activity) {
        ActivityJpaEntity saved = activityRepository.saveAndFlush(activity);
        entityManager.clear();
        return saved;
    }

    private static Activity 입금활동(AccountId owner, AccountId source,
                                  LocalDateTime timestamp, long amount) {
        return new Activity(owner, source, owner, timestamp, Money.of(amount));
    }

    private static Activity 출금활동(AccountId owner, AccountId target,
                                  LocalDateTime timestamp, long amount) {
        return new Activity(owner, owner, target, timestamp, Money.of(amount));
    }

    private static Account 계좌(AccountId accountId, Activity... activities) {
        return Account.withId(accountId, Money.ZERO, new ActivityWindow(activities));
    }

    private List<ActivityJpaEntity> 저장된_활동들() {
        entityManager.flush();
        entityManager.clear();
        return activityRepository.findAll();
    }

    @Nested
    class 계좌_로드 {

        @Test
        void 기준시각_이후의_활동을_윈도우에_담는다() {
            저장된행(입금행(계좌_A, 계좌_B, 기준시각.plusDays(1), 100L));

            Account account = adapter.loadAccount(계좌_A, 기준시각);

            assertThat(account.getActivityWindow().activities())
                    .extracting(Activity::money)
                    .containsExactly(Money.of(100L));
        }

        @Test
        void 기준시각_이전의_활동은_윈도우에_담기지_않는다() {
            저장된행(입금행(계좌_A, 계좌_B, 기준시각.minusDays(1), 100L));

            Account account = adapter.loadAccount(계좌_A, 기준시각);

            assertThat(account.getActivityWindow().activities()).isEmpty();
        }

        @Test
        void 기준시각과_같은_시각의_활동도_윈도우에_담긴다() {
            저장된행(입금행(계좌_A, 계좌_B, 기준시각, 100L));

            Account account = adapter.loadAccount(계좌_A, 기준시각);

            assertThat(account.getActivityWindow().activities()).hasSize(1);
        }

        @Test
        void 다른_계좌가_소유한_활동은_윈도우에_담기지_않는다() {
            저장된행(입금행(계좌_B, 계좌_A, 기준시각, 100L));

            Account account = adapter.loadAccount(계좌_A, 기준시각);

            assertThat(account.getActivityWindow().activities()).isEmpty();
        }

        @Test
        void 윈도우의_활동은_저장된_행의_ID를_가진다() {
            ActivityJpaEntity saved = 저장된행(입금행(계좌_A, 계좌_B, 기준시각, 100L));

            Account account = adapter.loadAccount(계좌_A, 기준시각);

            assertThat(account.getActivityWindow().activities())
                    .extracting(Activity::id)
                    .containsExactly(new ActivityId(saved.getId()));
        }

        @Test
        void 활동이_없으면_잔액은_ZERO다() {
            Account account = adapter.loadAccount(계좌_A, 기준시각);

            assertThat(account.calculateBalance()).isEqualTo(Money.ZERO);
        }

        @Test
        void 기준시각_이전의_입금에서_출금을_뺀_값이_기준_잔액이_된다() {
            저장된행(입금행(계좌_A, 계좌_B, 기준시각.minusDays(2), 500L));
            저장된행(출금행(계좌_A, 계좌_B, 기준시각.minusDays(1), 100L));

            Account account = adapter.loadAccount(계좌_A, 기준시각);

            assertThat(account.calculateBalance()).isEqualTo(Money.of(400L));
        }

        @Test
        void 기준시각_이후의_활동은_기준_잔액에_더해진다() {
            저장된행(입금행(계좌_A, 계좌_B, 기준시각.minusDays(1), 500L));
            저장된행(입금행(계좌_A, 계좌_B, 기준시각.plusDays(1), 300L));
            저장된행(출금행(계좌_A, 계좌_B, 기준시각.plusDays(2), 100L));

            Account account = adapter.loadAccount(계좌_A, 기준시각);

            assertThat(account.calculateBalance()).isEqualTo(Money.of(700L));
        }

        @Test
        void 출금이_입금보다_많으면_잔액이_음수가_된다() {
            저장된행(입금행(계좌_A, 계좌_B, 기준시각.minusDays(2), 100L));
            저장된행(출금행(계좌_A, 계좌_B, 기준시각.minusDays(1), 500L));

            Account account = adapter.loadAccount(계좌_A, 기준시각);

            assertThat(account.calculateBalance()).isEqualTo(Money.of(-400L));
        }

        @Test
        void 다른_계좌가_소유한_활동은_잔액에_반영되지_않는다() {
            저장된행(입금행(계좌_B, 계좌_A, 기준시각.minusDays(1), 300L));

            Account account = adapter.loadAccount(계좌_A, 기준시각);

            assertThat(account.calculateBalance()).isEqualTo(Money.ZERO);
        }

        @Test
        void 없는_계좌_ID로_로드하면_예외가_발생한다() {
            assertThatThrownBy(() -> adapter.loadAccount(없는_계좌, 기준시각))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    class 활동_갱신 {

        @Test
        void ID가_없는_활동을_저장한다() {
            Account account = 계좌(계좌_A, 입금활동(계좌_A, 계좌_B, 기준시각, 100L));

            adapter.updateActivities(account);

            assertThat(저장된_활동들()).hasSize(1);
        }

        @Test
        void 저장한_활동의_모든_필드가_행에_담긴다() {
            Account account = 계좌(계좌_A, 출금활동(계좌_A, 계좌_B, 기준시각, 100L));

            adapter.updateActivities(account);

            ActivityJpaEntity row = 저장된_활동들().getFirst();
            assertThat(row.getTimestamp()).isEqualTo(기준시각);
            assertThat(row.getOwnerAccountId()).isEqualTo(계좌_A.value());
            assertThat(row.getSourceAccountId()).isEqualTo(계좌_A.value());
            assertThat(row.getTargetAccountId()).isEqualTo(계좌_B.value());
            assertThat(row.getAmount()).isEqualTo(100L);
        }

        @Test
        void 저장한_활동에는_ID가_생성된다() {
            Account account = 계좌(계좌_A, 입금활동(계좌_A, 계좌_B, 기준시각, 100L));

            adapter.updateActivities(account);

            assertThat(저장된_활동들().getFirst().getId()).isNotNull();
        }

        @Test
        void ID가_있는_활동은_다시_저장하지_않는다() {
            ActivityJpaEntity saved = 저장된행(입금행(계좌_A, 계좌_B, 기준시각, 100L));
            Activity 이미_저장된_활동 = new Activity(
                    new ActivityId(saved.getId()), 계좌_A, 계좌_B, 계좌_A, 기준시각, Money.of(100L));

            adapter.updateActivities(계좌(계좌_A, 이미_저장된_활동));

            assertThat(저장된_활동들()).hasSize(1);
        }

        @Test
        void ID가_없는_활동만_골라_저장한다() {
            ActivityJpaEntity saved = 저장된행(입금행(계좌_A, 계좌_B, 기준시각, 100L));
            Activity 이미_저장된_활동 = new Activity(
                    new ActivityId(saved.getId()), 계좌_A, 계좌_B, 계좌_A, 기준시각, Money.of(100L));
            Activity 새_활동 = 입금활동(계좌_A, 계좌_B, 기준시각.plusDays(1), 200L);

            adapter.updateActivities(계좌(계좌_A, 이미_저장된_활동, 새_활동));

            assertThat(저장된_활동들())
                    .extracting(ActivityJpaEntity::getAmount)
                    .containsExactlyInAnyOrder(100L, 200L);
        }

        @Test
        void 여러_활동을_한번에_저장한다() {
            Account account = 계좌(
                    계좌_A,
                    입금활동(계좌_A, 계좌_B, 기준시각, 100L),
                    출금활동(계좌_A, 계좌_B, 기준시각.plusDays(1), 200L));

            adapter.updateActivities(account);

            assertThat(저장된_활동들()).hasSize(2);
        }

        @Test
        void 활동이_없으면_아무것도_저장하지_않는다() {
            adapter.updateActivities(계좌(계좌_A));

            assertThat(저장된_활동들()).isEmpty();
        }
    }

    @Nested
    class 저장_후_로드 {

        @Test
        void 저장한_활동이_다시_로드한_계좌의_잔액에_반영된다() {
            adapter.updateActivities(계좌(계좌_A, 입금활동(계좌_A, 계좌_B, 기준시각.plusDays(1), 300L)));
            entityManager.flush();
            entityManager.clear();

            Account account = adapter.loadAccount(계좌_A, 기준시각);

            assertThat(account.calculateBalance()).isEqualTo(Money.of(300L));
        }

        @Test
        void 저장한_활동은_다시_로드하면_ID를_가진다() {
            adapter.updateActivities(계좌(계좌_A, 입금활동(계좌_A, 계좌_B, 기준시각.plusDays(1), 300L)));
            entityManager.flush();
            entityManager.clear();

            Account account = adapter.loadAccount(계좌_A, 기준시각);

            assertThat(account.getActivityWindow().activities())
                    .allSatisfy(activity -> assertThat(activity.id()).isNotNull());
        }
    }
}
