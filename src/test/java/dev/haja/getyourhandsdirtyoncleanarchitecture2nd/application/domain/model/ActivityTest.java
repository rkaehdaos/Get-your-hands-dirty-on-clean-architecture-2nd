package dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.model;

import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.model.Account.AccountId;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ActivityTest {

    private static final AccountId OWNER = new AccountId(1L);
    private static final AccountId SOURCE = new AccountId(2L);
    private static final AccountId TARGET = new AccountId(3L);
    private static final LocalDateTime TIMESTAMP = LocalDateTime.of(2026, 9, 7, 12, 0);
    private static final Money MONEY = Money.of(1000L);

    @Nested
    class 생성 {

        @Test
        void 모든_값을_전달하면_각_컴포넌트에_그대로_담긴다() {
            Activity.ActivityId id = new Activity.ActivityId(100L);

            Activity activity = new Activity(id, OWNER, SOURCE, TARGET, TIMESTAMP, MONEY);

            assertThat(activity.id()).isEqualTo(id);
            assertThat(activity.ownerAccountId()).isEqualTo(OWNER);
            assertThat(activity.sourceAccountId()).isEqualTo(SOURCE);
            assertThat(activity.targetAccountId()).isEqualTo(TARGET);
            assertThat(activity.timestamp()).isEqualTo(TIMESTAMP);
            assertThat(activity.money()).isEqualTo(MONEY);
        }

        @Test
        void id는_null이어도_생성된다() {
            Activity activity = new Activity(null, OWNER, SOURCE, TARGET, TIMESTAMP, MONEY);

            assertThat(activity.id()).isNull();
        }
    }

    @Nested
    class 보조_생성자 {

        @Test
        void id_없이_생성하면_id는_null이다() {
            Activity activity = new Activity(OWNER, SOURCE, TARGET, TIMESTAMP, MONEY);

            assertThat(activity.id()).isNull();
        }

        @Test
        void 나머지_컴포넌트는_전달한_값과_같다() {
            Activity activity = new Activity(OWNER, SOURCE, TARGET, TIMESTAMP, MONEY);

            assertThat(activity).isEqualTo(new Activity(null, OWNER, SOURCE, TARGET, TIMESTAMP, MONEY));
        }
    }

    @Nested
    class null_검증 {

        @Test
        void ownerAccountId가_null이면_예외가_발생한다() {
            assertThatThrownBy(() -> new Activity(null, null, SOURCE, TARGET, TIMESTAMP, MONEY))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        void sourceAccountId가_null이면_예외가_발생한다() {
            assertThatThrownBy(() -> new Activity(null, OWNER, null, TARGET, TIMESTAMP, MONEY))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        void targetAccountId가_null이면_예외가_발생한다() {
            assertThatThrownBy(() -> new Activity(null, OWNER, SOURCE, null, TIMESTAMP, MONEY))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        void timestamp가_null이면_예외가_발생한다() {
            assertThatThrownBy(() -> new Activity(null, OWNER, SOURCE, TARGET, null, MONEY))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        void money가_null이면_예외가_발생한다() {
            assertThatThrownBy(() -> new Activity(null, OWNER, SOURCE, TARGET, TIMESTAMP, null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        void 보조_생성자로_생성해도_null이면_예외가_발생한다() {
            assertThatThrownBy(() -> new Activity(OWNER, SOURCE, TARGET, TIMESTAMP, null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    class 동등성 {

        @Test
        void 모든_컴포넌트가_같으면_동등하다() {
            Activity a = new Activity(OWNER, SOURCE, TARGET, TIMESTAMP, MONEY);
            Activity b = new Activity(OWNER, SOURCE, TARGET, TIMESTAMP, MONEY);

            assertThat(a).isEqualTo(b);
            assertThat(a).hasSameHashCodeAs(b);
        }

        @Test
        void money가_다르면_동등하지_않다() {
            Activity a = new Activity(OWNER, SOURCE, TARGET, TIMESTAMP, MONEY);
            Activity b = new Activity(OWNER, SOURCE, TARGET, TIMESTAMP, Money.of(2000L));

            assertThat(a).isNotEqualTo(b);
        }
    }

    @Nested
    class ActivityId {

        @Test
        void value를_그대로_보관한다() {
            Activity.ActivityId id = new Activity.ActivityId(100L);

            assertThat(id.value()).isEqualTo(100L);
        }

        @Test
        void 같은_value면_동등하다() {
            assertThat(new Activity.ActivityId(100L)).isEqualTo(new Activity.ActivityId(100L));
        }

        @Test
        void value가_null이어도_생성된다() {
            Activity.ActivityId id = new Activity.ActivityId(null);

            assertThat(id.value()).isNull();
        }
    }
}
