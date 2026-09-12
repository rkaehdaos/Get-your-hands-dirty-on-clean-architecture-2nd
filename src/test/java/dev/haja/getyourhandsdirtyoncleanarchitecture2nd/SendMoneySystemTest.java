package dev.haja.getyourhandsdirtyoncleanarchitecture2nd;

import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.model.Account;
import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.model.Account.AccountId;
import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.model.Money;
import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.port.out.LoadAccountPort;

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
import java.time.LocalDateTime;

import static org.assertj.core.api.BDDAssertions.then;

@AutoConfigureTestRestTemplate
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class SendMoneySystemTest {

    @Autowired private TestRestTemplate testRestTemplate;
    @Autowired private LoadAccountPort loadAccountPort;

    @Test
    @Sql("SendMoneySystemTest.sql")
    void sendMoney() {

        // given
        AccountId sourceAccountId = new AccountId(1L);
        AccountId targetAccountId = new AccountId(2L);
        Account sourceAccount = loadAccount(sourceAccountId);
        Account targetAccount = loadAccount(targetAccountId);
        Money transferredAmount = Money.of(500L);
        Money initialSourceBalance = sourceAccount.calculateBalance();
        Money initialTargetBalance = targetAccount.calculateBalance();

        // when
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        HttpEntity<Void> request = new HttpEntity<>(null, headers);
        ResponseEntity<Object> response = testRestTemplate.exchange(
                "/accounts/send/{sourceAccountId}/{targetAccountId}/{amount}",
                HttpMethod.POST,
                request,
                Object.class,
                sourceAccountId.value(),
                targetAccountId.value(),
                transferredAmount.amount());

        // then
        then(response.getStatusCode())
                .isEqualTo(HttpStatus.OK);
        // DB와 연결된 살아있는 계좌 엔티티 다시 로드
        sourceAccount = loadAccount(sourceAccountId);
        targetAccount = loadAccount(targetAccountId);
        then(sourceAccount.calculateBalance())
                .isEqualTo(initialSourceBalance.minus(transferredAmount));

        then(targetAccount.calculateBalance())
                .isEqualTo(initialTargetBalance.plus(transferredAmount));

    }

    private Account loadAccount(AccountId accountId) {
        return loadAccountPort.loadAccount(
                accountId,
                LocalDateTime.now());
    }

}
