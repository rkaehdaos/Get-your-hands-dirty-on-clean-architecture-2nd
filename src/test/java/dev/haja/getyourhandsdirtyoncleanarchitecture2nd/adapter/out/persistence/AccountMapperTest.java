package dev.haja.getyourhandsdirtyoncleanarchitecture2nd.adapter.out.persistence;

import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.model.Account;
import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.model.Account.AccountId;
import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.model.Activity;
import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.model.Activity.ActivityId;
import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.model.ActivityWindow;
import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.model.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AccountMapperTest {

    private static final long 계좌_A_ID = 1L;
    private static final long 계좌_B_ID = 2L;
    private static final AccountId 계좌_A = new AccountId(계좌_A_ID);
    private static final AccountId 계좌_B = new AccountId(계좌_B_ID);
    private static final LocalDateTime T1 = LocalDateTime.of(2026, 1, 1, 10, 0);
    private static final LocalDateTime T2 = LocalDateTime.of(2026, 1, 1, 11, 0);

    private AccountMapper accountMapper;

    @BeforeEach
    void setUp() {
        accountMapper = new AccountMapper();
    }

    private static AccountJpaEntity 계좌행(long id) {
        return new AccountJpaEntity(id);
    }

    private static ActivityJpaEntity 입금행(Long id, LocalDateTime timestamp, long amount) {
        return new ActivityJpaEntity(id, timestamp, 계좌_A_ID, 계좌_B_ID, 계좌_A_ID, amount);
    }

    private static ActivityJpaEntity 출금행(Long id, LocalDateTime timestamp, long amount) {
        return new ActivityJpaEntity(id, timestamp, 계좌_A_ID, 계좌_A_ID, 계좌_B_ID, amount);
    }

    private static Activity 입금활동(Long id, LocalDateTime timestamp, long amount) {
        return 입금활동(id, timestamp, Money.of(amount));
    }

    private static Activity 입금활동(Long id, LocalDateTime timestamp, Money money) {
        return new Activity(new ActivityId(id), 계좌_A, 계좌_B, 계좌_A, timestamp, money);
    }

    private static Money long_범위_밖_금액(long boundary, int offset) {
        return new Money(BigInteger.valueOf(boundary).add(BigInteger.valueOf(offset)));
    }

    @Nested
    class 활동_윈도우_매핑 {

        @Test
        void 활동_행의_모든_필드를_매핑한다() {
            ActivityWindow window = accountMapper.mapToActivityWindow(List.of(입금행(10L, T1, 100L)));

            assertThat(window.activities()).containsExactly(입금활동(10L, T1, 100L));
        }

        @Test
        void 여러_활동_행을_순서대로_매핑한다() {
            ActivityWindow window = accountMapper.mapToActivityWindow(
                    List.of(입금행(10L, T1, 100L), 입금행(11L, T2, 200L)));

            assertThat(window.activities())
                    .containsExactly(입금활동(10L, T1, 100L), 입금활동(11L, T2, 200L));
        }

        @Test
        void 출금_행은_source가_소유_계좌인_활동이_된다() {
            ActivityWindow window = accountMapper.mapToActivityWindow(List.of(출금행(10L, T1, 100L)));

            assertThat(window.calculateBalance(계좌_A)).isEqualTo(Money.of(-100L));
        }

        @Test
        void 빈_리스트는_빈_윈도우가_된다() {
            ActivityWindow window = accountMapper.mapToActivityWindow(List.of());

            assertThat(window.activities()).isEmpty();
        }

        @Test
        void 활동_행의_id가_null이면_예외가_발생한다() {
            List<ActivityJpaEntity> activities = List.of(입금행(null, T1, 100L));

            assertThatThrownBy(() -> accountMapper.mapToActivityWindow(activities))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("value");
        }

        @Test
        void 소유_계좌가_source도_target도_아니면_예외가_발생한다() {
            List<ActivityJpaEntity> activities =
                    List.of(new ActivityJpaEntity(10L, T1, 계좌_A_ID, 계좌_B_ID, 3L, 100L));

            assertThatThrownBy(() -> accountMapper.mapToActivityWindow(activities))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("ownerAccountId");
        }
    }

    @Nested
    class JPA_엔티티_매핑 {

        @Test
        void 활동의_모든_필드를_JPA_엔티티로_옮긴다() {
            ActivityJpaEntity entity = accountMapper.mapToJpaEntity(입금활동(10L, T1, 100L));

            assertThat(entity.getId()).isEqualTo(10L);
            assertThat(entity.getTimestamp()).isEqualTo(T1);
            assertThat(entity.getOwnerAccountId()).isEqualTo(계좌_A_ID);
            assertThat(entity.getSourceAccountId()).isEqualTo(계좌_B_ID);
            assertThat(entity.getTargetAccountId()).isEqualTo(계좌_A_ID);
            assertThat(entity.getAmount()).isEqualTo(100L);
        }

        @Test
        void id가_없는_활동은_엔티티_id가_null이_된다() {
            Activity activity = new Activity(계좌_A, 계좌_B, 계좌_A, T1, Money.of(100L));

            ActivityJpaEntity entity = accountMapper.mapToJpaEntity(activity);

            assertThat(entity.getId()).isNull();
        }

        @Test
        void long_범위_경계의_금액은_그대로_옮긴다() {
            ActivityJpaEntity 최댓값 = accountMapper.mapToJpaEntity(입금활동(10L, T1, Long.MAX_VALUE));
            ActivityJpaEntity 최솟값 = accountMapper.mapToJpaEntity(입금활동(11L, T1, Long.MIN_VALUE));

            assertThat(최댓값.getAmount()).isEqualTo(Long.MAX_VALUE);
            assertThat(최솟값.getAmount()).isEqualTo(Long.MIN_VALUE);
        }

        @Test
        void long_범위를_넘는_금액은_예외가_발생한다() {
            Activity activity = 입금활동(10L, T1, long_범위_밖_금액(Long.MAX_VALUE, 1));

            assertThatThrownBy(() -> accountMapper.mapToJpaEntity(activity))
                    .isInstanceOf(ArithmeticException.class);
        }

        @Test
        void long_범위_아래의_금액은_예외가_발생한다() {
            Activity activity = 입금활동(10L, T1, long_범위_밖_금액(Long.MIN_VALUE, -1));

            assertThatThrownBy(() -> accountMapper.mapToJpaEntity(activity))
                    .isInstanceOf(ArithmeticException.class);
        }

        @Test
        void 매핑한_엔티티를_다시_매핑하면_원래_활동이_된다() {
            Activity activity = 입금활동(10L, T1, 100L);

            ActivityWindow window = accountMapper.mapToActivityWindow(
                    List.of(accountMapper.mapToJpaEntity(activity)));

            assertThat(window.activities()).containsExactly(activity);
        }
    }

    @Nested
    class 계좌_매핑 {

        @Test
        void 활동이_없으면_입금_잔액에서_출금_잔액을_뺀_값이_잔액이_된다() {
            Account account = accountMapper.mapToDomainEntity(계좌행(계좌_A_ID), List.of(), 100L, 500L);

            assertThat(account.calculateBalance()).isEqualTo(Money.of(400L));
        }

        @Test
        void 출금_잔액이_더_크면_기준_잔액이_음수가_된다() {
            Account account = accountMapper.mapToDomainEntity(계좌행(계좌_A_ID), List.of(), 500L, 100L);

            assertThat(account.calculateBalance()).isEqualTo(Money.of(-400L));
        }

        @Test
        void 활동_행의_잔액이_기준_잔액에_더해진다() {
            Account account = accountMapper.mapToDomainEntity(
                    계좌행(계좌_A_ID),
                    List.of(입금행(10L, T1, 300L), 출금행(11L, T2, 100L)),
                    0L, 500L);

            assertThat(account.calculateBalance()).isEqualTo(Money.of(700L));
        }

        @Test
        void 다른_계좌가_소유한_활동_행은_잔액에_반영되지_않는다() {
            ActivityJpaEntity 계좌_B의_입금 =
                    new ActivityJpaEntity(10L, T1, 계좌_B_ID, 3L, 계좌_B_ID, 300L);

            Account account = accountMapper.mapToDomainEntity(
                    계좌행(계좌_A_ID), List.of(계좌_B의_입금), 0L, 500L);

            assertThat(account.calculateBalance()).isEqualTo(Money.of(500L));
        }

        @Test
        void 계좌_행의_id가_null이면_예외가_발생한다() {
            AccountJpaEntity account = new AccountJpaEntity(null);

            assertThatThrownBy(() -> accountMapper.mapToDomainEntity(account, List.of(), 0L, 0L))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("value");
        }
    }
}
