package dev.haja.getyourhandsdirtyoncleanarchitecture2nd;

import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.model.Account;
import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.model.Account.AccountId;
import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.model.Money;
import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.port.out.LoadAccountPort;
import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.common.AccountFixture;

import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.jdbc.Sql;

import org.junit.jupiter.api.Test;

import static dev.haja.getyourhandsdirtyoncleanarchitecture2nd.common.AccountFixture.BASELINE_DATE;
import static dev.haja.getyourhandsdirtyoncleanarchitecture2nd.common.AccountFixture.SOURCE_ACCOUNT_ID;
import static dev.haja.getyourhandsdirtyoncleanarchitecture2nd.common.AccountFixture.TARGET_ACCOUNT_ID;
import static org.assertj.core.api.BDDAssertions.then;

@AutoConfigureTestRestTemplate
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class SendMoneySystemTest {

    @Autowired private TestRestTemplate testRestTemplate;
    @Autowired private LoadAccountPort loadAccountPort;

    @Test
    @Sql(AccountFixture.SQL)
    void sendMoney() {

        // given
        Money initialSourceBalance = sourceAccount().calculateBalance();
        Money initialTargetBalance = targetAccount().calculateBalance();

        // 이체가 잔액 한계선에 걸치지 않는다 — 500을 보내고도 500이 남는다
        then(initialSourceBalance).isEqualTo(Money.of(1000L));

        // when
        ResponseEntity<Object> response = whenSendMoney(
                SOURCE_ACCOUNT_ID,
                TARGET_ACCOUNT_ID,
                transferredAmount());

        // then
        then(response.getStatusCode())
                .isEqualTo(HttpStatus.OK);

        then(sourceAccount().calculateBalance())
                .isEqualTo(initialSourceBalance.minus(transferredAmount()));

        then(targetAccount().calculateBalance())
                .isEqualTo(initialTargetBalance.plus(transferredAmount()));

    }

    private Account sourceAccount() {
        return loadAccount(SOURCE_ACCOUNT_ID);
    }

    private Account targetAccount() {
        return loadAccount(TARGET_ACCOUNT_ID);
    }

    private Account loadAccount(AccountId accountId) {
        return loadAccountPort.loadAccount(accountId, BASELINE_DATE);
    }

    private ResponseEntity<Object> whenSendMoney(AccountId sourceAccountId, AccountId targetAccountId, Money transferredAmount) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        HttpEntity<Void> request = new HttpEntity<>(null, headers);
        return testRestTemplate.exchange(
                "/accounts/send/{sourceAccountId}/{targetAccountId}/{amount}",
                HttpMethod.POST,
                request,
                Object.class,
                sourceAccountId.value(),
                targetAccountId.value(),
                transferredAmount.amount());
    }

    private Money transferredAmount() {
        return Money.of(500L);
    }

}
