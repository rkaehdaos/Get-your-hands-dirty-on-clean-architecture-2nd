package dev.haja.getyourhandsdirtyoncleanarchitecture2nd.adapter.in.web;

import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.model.Account.AccountId;
import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.model.Money;
import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.port.in.SendMoneyCommand;
import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.port.in.SendMoneyUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import static org.mockito.BDDMockito.eq;
import static org.mockito.BDDMockito.then;
import static org.assertj.core.api.Assertions.assertThat;

@WebMvcTest(controllers = SendMoneyController.class)
class SendMoneyControllerTest {

    @Autowired private MockMvcTester mockMvcTester;
    @MockitoBean private SendMoneyUseCase sendMoneyUseCase;

    @Test
    void testSendMoney() {

        // when
        var result = mockMvcTester.post()
                .uri("/accounts/send/{sourceAccountId}/{targetAccountId}/{amount}", 41L, 42L, 500)
                .header("Content-Type", "application/json")
                .exchange();

        // then
        assertThat(result).hasStatus(HttpStatus.OK);

        then(sendMoneyUseCase).should()
                .sendMoney(eq(new SendMoneyCommand(
                        new AccountId(41L),
                        new AccountId(42L),
                        Money.of(500L))));
    }
}