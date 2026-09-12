package dev.haja.getyourhandsdirtyoncleanarchitecture2nd;

import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.model.Account;
import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.model.Account.AccountId;

import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.port.out.LoadAccountPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@AutoConfigureTestRestTemplate
@SpringBootTest(webEnvironment = RANDOM_PORT)
class SendMoneySystemTest {

    @Autowired private TestRestTemplate testRestTemplate;
    @Autowired private LoadAccountPort loadAccountPort;

    @Test
    @Sql("SendMoneySystemTest.sql")
    void sendMoney() {

        // given
        AccountId sourceAccountId = new AccountId(1L);
        AccountId targetAccountId = new AccountId(2L);

        // when

        // then

    }

    private Account loadAccount(AccountId accountId) {
        return loadAccountPort.loadAccount(
                accountId,
                LocalDateTime.now());
    }

}
