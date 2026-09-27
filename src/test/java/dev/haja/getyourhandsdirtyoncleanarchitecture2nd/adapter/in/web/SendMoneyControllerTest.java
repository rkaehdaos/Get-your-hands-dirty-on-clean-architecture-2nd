package dev.haja.getyourhandsdirtyoncleanarchitecture2nd.adapter.in.web;

import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.model.Account.AccountId;
import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.model.Money;
import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.port.in.InsufficientFundsException;
import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.port.in.NoSuchAccountException;
import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.port.in.SendMoneyCommand;
import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.port.in.SendMoneyUseCase;
import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.port.in.ThresholdExceededException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledInNativeImage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.test.context.aot.DisabledInAotMode;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.client.RestTestClient;
import static dev.haja.getyourhandsdirtyoncleanarchitecture2nd.common.AccountTestData.DEFAULT_ACCOUNT_ID;
import static dev.haja.getyourhandsdirtyoncleanarchitecture2nd.common.AccountTestData.OTHER_ACCOUNT_ID;
import static org.mockito.BDDMockito.any;
import static org.mockito.BDDMockito.eq;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.assertj.core.api.Assertions.assertThat;

// 네이티브 이미지는 런타임 바이트코드 생성을 지원하지 않아 Mockito가 초기화되지 않는다.
// HTTP 매핑은 JVM 테스트로 고정하고, 네이티브의 송금 경로는 SendMoneySystemTest가 맡는다.
// @DisabledInNativeImage는 실행만 막을 뿐 이 컨텍스트는 여전히 AOT 처리되어 이미지에 실린다.
// 돌지 않을 컨텍스트이므로 @DisabledInAotMode로 AOT 처리에서도 뺀다.
@DisabledInNativeImage
@DisabledInAotMode
@WebMvcTest(controllers = SendMoneyController.class)
@AutoConfigureRestTestClient
class SendMoneyControllerTest {

    @Autowired private RestTestClient restTestClient;
    @MockitoBean private SendMoneyUseCase sendMoneyUseCase;

    @Test
    void testSendMoney() {

        // when
        var response = restTestClient.post()
                .uri("/accounts/send/{sourceAccountId}/{targetAccountId}/{amount}",
                        OTHER_ACCOUNT_ID.value(), DEFAULT_ACCOUNT_ID.value(), 500)
                .contentType(MediaType.APPLICATION_JSON)
                .exchange();

        // then
        response.expectStatus().isOk();

        then(sendMoneyUseCase).should()
                .sendMoney(eq(new SendMoneyCommand(
                        OTHER_ACCOUNT_ID,
                        DEFAULT_ACCOUNT_ID,
                        Money.of(500L))));
    }

    @Test
    @DisplayName("잔액 부족으로 이체가 거부되면 422 Unprocessable Entity가 응답됨")
    void givenInsufficientFunds_thenRespondsWithUnprocessableEntity() {

        // given
        willThrow(new InsufficientFundsException(OTHER_ACCOUNT_ID, Money.of(500L)))
                .given(sendMoneyUseCase).sendMoney(any(SendMoneyCommand.class));

        // when
        var response = restTestClient.post()
                .uri("/accounts/send/{sourceAccountId}/{targetAccountId}/{amount}",
                        OTHER_ACCOUNT_ID.value(), DEFAULT_ACCOUNT_ID.value(), 500)
                .contentType(MediaType.APPLICATION_JSON)
                .exchange();

        // then
        response.expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT)
                .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON);

        ProblemDetail problem = response.expectBody(ProblemDetail.class)
                .returnResult()
                .getResponseBody();
        assertThat(problem.getDetail()).contains("41", "500");
    }

    @Test
    @DisplayName("한도를 초과한 이체가 거부되면 422 Unprocessable Entity가 응답됨")
    void givenThresholdExceeded_thenRespondsWithUnprocessableEntity() {

        // given
        willThrow(new ThresholdExceededException(Money.of(1_000_000L), Money.of(2_000_000L)))
                .given(sendMoneyUseCase).sendMoney(any(SendMoneyCommand.class));

        // when
        var response = restTestClient.post()
                .uri("/accounts/send/{sourceAccountId}/{targetAccountId}/{amount}",
                        OTHER_ACCOUNT_ID.value(), DEFAULT_ACCOUNT_ID.value(), 2_000_000)
                .contentType(MediaType.APPLICATION_JSON)
                .exchange();

        // then
        response.expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT)
                .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON);

        ProblemDetail problem = response.expectBody(ProblemDetail.class)
                .returnResult()
                .getResponseBody();
        assertThat(problem.getDetail()).contains("1000000", "2000000");
    }

    @Test
    @DisplayName("계좌가 없으면 404 Not Found가 응답됨")
    void givenNoSuchAccount_thenRespondsWithNotFound() {

        // given
        willThrow(new NoSuchAccountException(new AccountId(999L)))
                .given(sendMoneyUseCase).sendMoney(any(SendMoneyCommand.class));

        // when
        var response = restTestClient.post()
                .uri("/accounts/send/{sourceAccountId}/{targetAccountId}/{amount}",
                        999L, DEFAULT_ACCOUNT_ID.value(), 500)
                .contentType(MediaType.APPLICATION_JSON)
                .exchange();

        // then
        response.expectStatus().isNotFound()
                .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON);

        ProblemDetail problem = response.expectBody(ProblemDetail.class)
                .returnResult()
                .getResponseBody();
        assertThat(problem.getDetail()).contains("999");
    }

    @Test
    @DisplayName("금액이 양수가 아니면 커맨드 검증에 실패해 400 Bad Request가 응답됨")
    void givenNonPositiveAmount_thenRespondsWithBadRequest() {

        // when
        // 유스케이스 목을 스터빙할 필요가 없다 — 컨트롤러가 커맨드를 만드는 자리에서 터진다
        var response = restTestClient.post()
                .uri("/accounts/send/{sourceAccountId}/{targetAccountId}/{amount}",
                        OTHER_ACCOUNT_ID.value(), DEFAULT_ACCOUNT_ID.value(), 0)
                .contentType(MediaType.APPLICATION_JSON)
                .exchange();

        // then
        response.expectStatus().isBadRequest()
                .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON);

        ProblemDetail problem = response.expectBody(ProblemDetail.class)
                .returnResult()
                .getResponseBody();
        assertThat(problem.getDetail()).contains("money");

        then(sendMoneyUseCase).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("출금 계좌와 입금 계좌가 같으면 400 Bad Request가 응답됨")
    void givenSameSourceAndTargetAccount_thenRespondsWithBadRequest() {

        // when
        var response = restTestClient.post()
                .uri("/accounts/send/{sourceAccountId}/{targetAccountId}/{amount}",
                        DEFAULT_ACCOUNT_ID.value(), DEFAULT_ACCOUNT_ID.value(), 500)
                .contentType(MediaType.APPLICATION_JSON)
                .exchange();

        // then
        response.expectStatus().isBadRequest()
                .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON);

        ProblemDetail problem = response.expectBody(ProblemDetail.class)
                .returnResult()
                .getResponseBody();
        assertThat(problem.getDetail()).contains("targetAccountId");

        // 유스케이스에 닿지 않으므로 잠금도 원장 기록도 일어나지 않는다
        then(sendMoneyUseCase).shouldHaveNoInteractions();
    }
}
