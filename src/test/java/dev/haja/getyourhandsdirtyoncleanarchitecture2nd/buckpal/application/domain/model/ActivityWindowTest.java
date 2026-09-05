package dev.haja.getyourhandsdirtyoncleanarchitecture2nd.buckpal.application.domain.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class ActivityWindowTest {

    private final Account.AccountId sourceAccountId = new Account.AccountId(1L);
    private final Account.AccountId targetAccountId = new Account.AccountId(2L);

    private Activity defaultActivity(Account.AccountId source, Account.AccountId target, Money money) {
        return new Activity(source, source, target, LocalDateTime.now(), money);
    }

    @Test
    void null_활동을_담은_varargs_생성자는_NPE를_던진다() {
        Activity activity = defaultActivity(sourceAccountId, targetAccountId, Money.of(100L));

        assertThatNullPointerException()
                .isThrownBy(() -> new ActivityWindow(activity, null));
    }

    @Test
    void null_활동을_담은_리스트_생성자는_NPE를_던진다() {
        Activity activity = defaultActivity(sourceAccountId, targetAccountId, Money.of(100L));

        assertThatNullPointerException()
                .isThrownBy(() -> new ActivityWindow(Arrays.asList(activity, null)));
    }

    @Test
    void addActivity에_null을_넣으면_NPE를_던진다() {
        ActivityWindow activityWindow = new ActivityWindow(
                defaultActivity(sourceAccountId, targetAccountId, Money.of(100L)));

        assertThatNullPointerException()
                .isThrownBy(() -> activityWindow.addActivity(null));
    }

    @Test
    void 정상_활동으로_잔액을_계산한다() {
        Activity deposit = defaultActivity(sourceAccountId, targetAccountId, Money.of(500L));
        Activity withdrawal = defaultActivity(targetAccountId, sourceAccountId, Money.of(200L));
        ActivityWindow activityWindow = new ActivityWindow(deposit, withdrawal);

        Money balance = activityWindow.calculateBalance(targetAccountId);

        assertThat(balance).isEqualTo(Money.of(300L));
    }

    @Test
    void addActivity로_추가한_활동이_잔액에_반영된다() {
        ActivityWindow activityWindow = new ActivityWindow(
                defaultActivity(sourceAccountId, targetAccountId, Money.of(500L)));

        activityWindow.addActivity(
                defaultActivity(targetAccountId, sourceAccountId, Money.of(200L)));

        Money balance = activityWindow.calculateBalance(targetAccountId);
        assertThat(balance).isEqualTo(Money.of(300L));
    }
}
