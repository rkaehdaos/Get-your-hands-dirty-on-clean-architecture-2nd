package dev.haja.getyourhandsdirtyoncleanarchitecture2nd.common;


import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.model.Account;
import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.model.Account.AccountId;
import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.model.ActivityWindow;
import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.model.Money;

public class AccountTestData {

    /**
     * 빌더가 만드는 계좌의 ID. 활동의 소유자·출금 계좌도 이 계좌다.
     */
    public static final AccountId DEFAULT_ACCOUNT_ID = new AccountId(42L);

    /**
     * 이체 상대로 쓰는 두 번째 계좌의 ID. 기본 활동의 입금 계좌다.
     */
    public static final AccountId OTHER_ACCOUNT_ID = new AccountId(41L);

    public static AccountBuilder defaultAccount() {
        return new AccountBuilder()
                .withAccountId(DEFAULT_ACCOUNT_ID)
                .withBaselineBalance(Money.of(999L))
                .withActivityWindow(new ActivityWindow(
                        ActivityTestData.defaultActivity().build(),
                        ActivityTestData.defaultActivity().build()));
    }

    public static class AccountBuilder {

        private AccountId accountId;
        private Money baselineBalance;
        private ActivityWindow activityWindow;

        public AccountBuilder withAccountId(Account.AccountId accountId) {
            this.accountId = accountId;
            return this;
        }

        public AccountBuilder withBaselineBalance(Money baselineBalance) {
            this.baselineBalance = baselineBalance;
            return this;
        }

        public AccountBuilder withActivityWindow(ActivityWindow activityWindow) {
            this.activityWindow = activityWindow;
            return this;
        }

        public Account build() {
            return Account.withId(this.accountId, this.baselineBalance, this.activityWindow);
        }

    }
}
