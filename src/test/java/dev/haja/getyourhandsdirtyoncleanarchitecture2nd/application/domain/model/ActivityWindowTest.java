package dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.model;

import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.model.Account.AccountId;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ActivityWindowTest {

    private static final AccountId ACCOUNT_A = new AccountId(1L);
    private static final AccountId ACCOUNT_B = new AccountId(2L);
    private static final LocalDateTime T1 = LocalDateTime.of(2026, 9, 7, 10, 0);
    private static final LocalDateTime T2 = LocalDateTime.of(2026, 9, 7, 11, 0);
    private static final LocalDateTime T3 = LocalDateTime.of(2026, 9, 7, 12, 0);

    private static Activity activity(AccountId source, AccountId target, LocalDateTime timestamp, long amount) {
        return new Activity(source, source, target, timestamp, Money.of(amount));
    }

    @Nested
    class 생성 {

        @Test
        void 리스트_생성자로_만들면_전달한_활동을_순서대로_담는다() {
            Activity a1 = activity(ACCOUNT_A, ACCOUNT_B, T1, 100L);
            Activity a2 = activity(ACCOUNT_B, ACCOUNT_A, T2, 200L);

            ActivityWindow window = new ActivityWindow(List.of(a1, a2));

            assertThat(window.activities()).containsExactly(a1, a2);
        }

        @Test
        void 생성자에_넘긴_원본_리스트를_나중에_수정해도_윈도우는_변하지_않는다() {
            Activity a1 = activity(ACCOUNT_A, ACCOUNT_B, T1, 100L);
            List<Activity> source = new ArrayList<>(List.of(a1));

            ActivityWindow window = new ActivityWindow(source);
            source.add(activity(ACCOUNT_B, ACCOUNT_A, T2, 200L));

            assertThat(window.activities()).containsExactly(a1);
        }

        @Test
        void activities는_수정할_수_없다() {
            ActivityWindow window = new ActivityWindow(activity(ACCOUNT_A, ACCOUNT_B, T1, 100L));

            assertThatThrownBy(() -> window.activities().add(activity(ACCOUNT_B, ACCOUNT_A, T2, 200L)))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        void 가변인자_생성자는_리스트_생성자와_같은_결과가_된다() {
            Activity a1 = activity(ACCOUNT_A, ACCOUNT_B, T1, 100L);
            Activity a2 = activity(ACCOUNT_B, ACCOUNT_A, T2, 200L);

            ActivityWindow window = new ActivityWindow(a1, a2);

            assertThat(window).isEqualTo(new ActivityWindow(List.of(a1, a2)));
        }

        @Test
        void 인자_없이_생성하면_빈_윈도우가_된다() {
            ActivityWindow window = new ActivityWindow();

            assertThat(window.activities()).isEmpty();
        }
    }

    @Nested
    class null_검증 {

        @Test
        void 리스트가_null이면_예외가_발생한다() {
            assertThatThrownBy(() -> new ActivityWindow((List<Activity>) null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("activities");
        }

        @Test
        void 가변인자_배열이_null이면_예외가_발생한다() {
            assertThatThrownBy(() -> new ActivityWindow((Activity[]) null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("activities");
        }

        @Test
        void 활동_원소_중_null이_있으면_예외가_발생한다() {
            Activity activity = activity(ACCOUNT_A, ACCOUNT_B, T1, 100L);

            assertThatThrownBy(() -> new ActivityWindow(activity, null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    class addActivity {

        @Test
        void 활동을_추가하면_크기가_1_늘어난_새_윈도우가_반환된다() {
            ActivityWindow window = new ActivityWindow(activity(ACCOUNT_A, ACCOUNT_B, T1, 100L));

            ActivityWindow newWindow = window.addActivity(activity(ACCOUNT_B, ACCOUNT_A, T2, 200L));

            assertThat(newWindow.activities()).hasSize(2);
        }

        @Test
        void 원본_윈도우는_변하지_않는다() {
            Activity a1 = activity(ACCOUNT_A, ACCOUNT_B, T1, 100L);
            ActivityWindow window = new ActivityWindow(a1);

            ActivityWindow newWindow = window.addActivity(activity(ACCOUNT_B, ACCOUNT_A, T2, 200L));

            assertThat(window.activities()).containsExactly(a1);
            assertThat(newWindow.activities()).hasSize(2);
        }

        @Test
        void 추가한_활동이_리스트_마지막에_붙는다() {
            Activity a1 = activity(ACCOUNT_A, ACCOUNT_B, T1, 100L);
            Activity a2 = activity(ACCOUNT_B, ACCOUNT_A, T2, 200L);
            ActivityWindow window = new ActivityWindow(a1);

            ActivityWindow newWindow = window.addActivity(a2);

            assertThat(newWindow.activities()).containsExactly(a1, a2);
        }

        @Test
        void 빈_윈도우에_추가하면_활동_1개짜리_윈도우가_된다() {
            ActivityWindow window = new ActivityWindow();
            Activity a1 = activity(ACCOUNT_A, ACCOUNT_B, T1, 100L);

            ActivityWindow newWindow = window.addActivity(a1);

            assertThat(newWindow.activities()).containsExactly(a1);
        }
    }

    @Nested
    class 타임스탬프 {

        @Test
        void getStartTimestamp는_가장_이른_타임스탬프를_반환한다() {
            ActivityWindow window = new ActivityWindow(
                    activity(ACCOUNT_A, ACCOUNT_B, T2, 100L),
                    activity(ACCOUNT_B, ACCOUNT_A, T3, 200L),
                    activity(ACCOUNT_A, ACCOUNT_B, T1, 300L));

            assertThat(window.getStartTimestamp()).isEqualTo(T1);
        }

        @Test
        void getEndTimestamp는_가장_늦은_타임스탬프를_반환한다() {
            ActivityWindow window = new ActivityWindow(
                    activity(ACCOUNT_A, ACCOUNT_B, T2, 100L),
                    activity(ACCOUNT_B, ACCOUNT_A, T3, 200L),
                    activity(ACCOUNT_A, ACCOUNT_B, T1, 300L));

            assertThat(window.getEndTimestamp()).isEqualTo(T3);
        }

        @Test
        void 활동이_하나면_시작과_종료가_같다() {
            ActivityWindow window = new ActivityWindow(activity(ACCOUNT_A, ACCOUNT_B, T2, 100L));

            assertThat(window.getStartTimestamp()).isEqualTo(T2);
            assertThat(window.getEndTimestamp()).isEqualTo(T2);
        }

        @Test
        void 빈_윈도우에서_getStartTimestamp는_예외가_발생한다() {
            ActivityWindow window = new ActivityWindow();

            assertThatThrownBy(window::getStartTimestamp)
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        void 빈_윈도우에서_getEndTimestamp는_예외가_발생한다() {
            ActivityWindow window = new ActivityWindow();

            assertThatThrownBy(window::getEndTimestamp)
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    class 잔액_계산 {

        @Test
        void 입금만_있으면_합계가_그대로_양수_잔액이_된다() {
            ActivityWindow window = new ActivityWindow(
                    activity(ACCOUNT_B, ACCOUNT_A, T1, 100L),
                    activity(ACCOUNT_B, ACCOUNT_A, T2, 200L));

            assertThat(window.calculateBalance(ACCOUNT_A)).isEqualTo(Money.of(300L));
        }

        @Test
        void 출금만_있으면_음수_잔액이_된다() {
            ActivityWindow window = new ActivityWindow(
                    activity(ACCOUNT_A, ACCOUNT_B, T1, 100L),
                    activity(ACCOUNT_A, ACCOUNT_B, T2, 200L));

            assertThat(window.calculateBalance(ACCOUNT_A)).isEqualTo(Money.of(-300L));
        }

        @Test
        void 입금과_출금이_섞이면_차액이_반환된다() {
            ActivityWindow window = new ActivityWindow(
                    activity(ACCOUNT_B, ACCOUNT_A, T1, 500L),
                    activity(ACCOUNT_A, ACCOUNT_B, T2, 200L));

            assertThat(window.calculateBalance(ACCOUNT_A)).isEqualTo(Money.of(300L));
        }

        @Test
        void 관련_없는_계좌의_활동은_잔액에_반영되지_않는다() {
            AccountId unrelated = new AccountId(3L);
            ActivityWindow window = new ActivityWindow(
                    activity(ACCOUNT_B, unrelated, T1, 500L));

            assertThat(window.calculateBalance(ACCOUNT_A)).isEqualTo(Money.ZERO);
        }

        @Test
        void 빈_윈도우의_잔액은_ZERO다() {
            ActivityWindow window = new ActivityWindow();

            assertThat(window.calculateBalance(ACCOUNT_A)).isEqualTo(Money.ZERO);
        }

        @Test
        void source와_target이_같은_계좌인_활동은_상쇄되어_0이_된다() {
            ActivityWindow window = new ActivityWindow(activity(ACCOUNT_A, ACCOUNT_A, T1, 500L));

            assertThat(window.calculateBalance(ACCOUNT_A)).isEqualTo(Money.ZERO);
        }
    }

    @Nested
    class 동등성 {

        @Test
        void 같은_활동을_같은_순서로_담은_두_윈도우는_동등하다() {
            Activity a1 = activity(ACCOUNT_A, ACCOUNT_B, T1, 100L);
            Activity a2 = activity(ACCOUNT_B, ACCOUNT_A, T2, 200L);

            ActivityWindow window1 = new ActivityWindow(a1, a2);
            ActivityWindow window2 = new ActivityWindow(a1, a2);

            assertThat(window1).isEqualTo(window2);
            assertThat(window1).hasSameHashCodeAs(window2);
        }

        @Test
        void 활동_순서가_다르면_동등하지_않다() {
            Activity a1 = activity(ACCOUNT_A, ACCOUNT_B, T1, 100L);
            Activity a2 = activity(ACCOUNT_B, ACCOUNT_A, T2, 200L);

            ActivityWindow window1 = new ActivityWindow(a1, a2);
            ActivityWindow window2 = new ActivityWindow(a2, a1);

            assertThat(window1).isNotEqualTo(window2);
        }
    }
}
