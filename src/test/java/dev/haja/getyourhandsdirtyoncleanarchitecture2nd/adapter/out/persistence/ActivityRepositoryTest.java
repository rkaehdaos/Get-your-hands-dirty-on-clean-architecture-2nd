package dev.haja.getyourhandsdirtyoncleanarchitecture2nd.adapter.out.persistence;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ActivityRepositoryTest {

    private static final long 계좌_A = 1L;
    private static final long 계좌_B = 2L;
    private static final LocalDateTime 기준시각 = LocalDateTime.of(2026, 1, 1, 0, 0);

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private TestEntityManager entityManager;

    private static ActivityJpaEntity 입금활동(long ownerAccountId, long sourceAccountId,
                                          LocalDateTime timestamp, long amount) {
        return new ActivityJpaEntity(null, timestamp, ownerAccountId, sourceAccountId, ownerAccountId, amount);
    }

    private static ActivityJpaEntity 출금활동(long ownerAccountId, long targetAccountId,
                                          LocalDateTime timestamp, long amount) {
        return new ActivityJpaEntity(null, timestamp, ownerAccountId, ownerAccountId, targetAccountId, amount);
    }

    private ActivityJpaEntity 저장된활동(ActivityJpaEntity activity) {
        ActivityJpaEntity saved = activityRepository.saveAndFlush(activity);
        entityManager.clear();
        return saved;
    }

    private long 활동_테이블_행수() {
        entityManager.flush();
        return ((Number) entityManager.getEntityManager()
                .createNativeQuery("select count(*) from activity")
                .getSingleResult()).longValue();
    }

    @Nested
    class 저장 {

        @Test
        void 저장하면_ID가_생성된다() {
            ActivityJpaEntity saved = activityRepository.save(입금활동(계좌_A, 계좌_B, 기준시각, 100L));

            assertThat(saved.getId()).isNotNull();
        }

        @Test
        void 저장한_활동은_activity_테이블의_행이_된다() {
            activityRepository.save(입금활동(계좌_A, 계좌_B, 기준시각, 100L));

            assertThat(활동_테이블_행수()).isEqualTo(1L);
        }

        @Test
        void 저장한_활동을_ID로_조회한다() {
            ActivityJpaEntity saved = 저장된활동(입금활동(계좌_A, 계좌_B, 기준시각, 100L));

            Optional<ActivityJpaEntity> found = activityRepository.findById(saved.getId());

            assertThat(found).contains(saved);
        }
    }

    @Nested
    class 소유_계좌의_활동_조회 {

        @Test
        void since_이후의_활동만_조회한다() {
            저장된활동(입금활동(계좌_A, 계좌_B, 기준시각.minusDays(1), 100L));
            ActivityJpaEntity 이후 = 저장된활동(입금활동(계좌_A, 계좌_B, 기준시각.plusDays(1), 200L));

            List<ActivityJpaEntity> found = activityRepository.findByOwnerSince(계좌_A, 기준시각);

            assertThat(found).containsExactly(이후);
        }

        @Test
        void since와_같은_시각의_활동도_조회된다() {
            ActivityJpaEntity 경계 = 저장된활동(입금활동(계좌_A, 계좌_B, 기준시각, 100L));

            List<ActivityJpaEntity> found = activityRepository.findByOwnerSince(계좌_A, 기준시각);

            assertThat(found).containsExactly(경계);
        }

        @Test
        void 다른_계좌가_소유한_활동은_조회되지_않는다() {
            저장된활동(입금활동(계좌_B, 계좌_A, 기준시각, 100L));

            List<ActivityJpaEntity> found = activityRepository.findByOwnerSince(계좌_A, 기준시각);

            assertThat(found).isEmpty();
        }

        @Test
        void 입금과_출금을_모두_조회한다() {
            ActivityJpaEntity 입금 = 저장된활동(입금활동(계좌_A, 계좌_B, 기준시각, 100L));
            ActivityJpaEntity 출금 = 저장된활동(출금활동(계좌_A, 계좌_B, 기준시각, 200L));

            List<ActivityJpaEntity> found = activityRepository.findByOwnerSince(계좌_A, 기준시각);

            assertThat(found).containsExactlyInAnyOrder(입금, 출금);
        }

        @Test
        void 활동이_없으면_빈_리스트를_반환한다() {
            List<ActivityJpaEntity> found = activityRepository.findByOwnerSince(계좌_A, 기준시각);

            assertThat(found).isEmpty();
        }
    }

    @Nested
    class 입금_잔액_계산 {

        @Test
        void until_이전의_입금_금액을_합산한다() {
            저장된활동(입금활동(계좌_A, 계좌_B, 기준시각.minusDays(2), 100L));
            저장된활동(입금활동(계좌_A, 계좌_B, 기준시각.minusDays(1), 200L));

            Optional<Long> balance = activityRepository.getDepositBalanceUntil(계좌_A, 기준시각);

            assertThat(balance).contains(300L);
        }

        @Test
        void until과_같은_시각의_활동은_합산되지_않는다() {
            저장된활동(입금활동(계좌_A, 계좌_B, 기준시각, 100L));

            Optional<Long> balance = activityRepository.getDepositBalanceUntil(계좌_A, 기준시각);

            assertThat(balance).isEmpty();
        }

        @Test
        void 출금_활동은_합산되지_않는다() {
            저장된활동(출금활동(계좌_A, 계좌_B, 기준시각.minusDays(1), 100L));

            Optional<Long> balance = activityRepository.getDepositBalanceUntil(계좌_A, 기준시각);

            assertThat(balance).isEmpty();
        }

        @Test
        void 다른_계좌가_소유한_입금은_합산되지_않는다() {
            저장된활동(입금활동(계좌_B, 계좌_A, 기준시각.minusDays(1), 100L));

            Optional<Long> balance = activityRepository.getDepositBalanceUntil(계좌_A, 기준시각);

            assertThat(balance).isEmpty();
        }

        @Test
        void 입금이_없으면_빈_Optional을_반환한다() {
            Optional<Long> balance = activityRepository.getDepositBalanceUntil(계좌_A, 기준시각);

            assertThat(balance).isEmpty();
        }
    }

    @Nested
    class 출금_잔액_계산 {

        @Test
        void until_이전의_출금_금액을_합산한다() {
            저장된활동(출금활동(계좌_A, 계좌_B, 기준시각.minusDays(2), 100L));
            저장된활동(출금활동(계좌_A, 계좌_B, 기준시각.minusDays(1), 200L));

            Optional<Long> balance = activityRepository.getWithdrawalBalanceUntil(계좌_A, 기준시각);

            assertThat(balance).contains(300L);
        }

        @Test
        void until과_같은_시각의_활동은_합산되지_않는다() {
            저장된활동(출금활동(계좌_A, 계좌_B, 기준시각, 100L));

            Optional<Long> balance = activityRepository.getWithdrawalBalanceUntil(계좌_A, 기준시각);

            assertThat(balance).isEmpty();
        }

        @Test
        void 입금_활동은_합산되지_않는다() {
            저장된활동(입금활동(계좌_A, 계좌_B, 기준시각.minusDays(1), 100L));

            Optional<Long> balance = activityRepository.getWithdrawalBalanceUntil(계좌_A, 기준시각);

            assertThat(balance).isEmpty();
        }

        @Test
        void 다른_계좌가_소유한_출금은_합산되지_않는다() {
            저장된활동(출금활동(계좌_B, 계좌_A, 기준시각.minusDays(1), 100L));

            Optional<Long> balance = activityRepository.getWithdrawalBalanceUntil(계좌_A, 기준시각);

            assertThat(balance).isEmpty();
        }

        @Test
        void 출금이_없으면_빈_Optional을_반환한다() {
            Optional<Long> balance = activityRepository.getWithdrawalBalanceUntil(계좌_A, 기준시각);

            assertThat(balance).isEmpty();
        }
    }
}
