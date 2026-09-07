package dev.haja.getyourhandsdirtyoncleanarchitecture2nd.adapter.in.web;

import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.model.Account.AccountId;
import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.model.Money;
import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.port.in.SendMoneyCommand;
import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.port.in.SendMoneyUseCase;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;

@WebMvcTest(SendMoneyController.class)
class SendMoneyControllerTest {

    private static final String PATH = "/accounts/send/{sourceAccountId}/{targetAccountId}/{amount}";
    private static final long SOURCE_ID = 41L;
    private static final long TARGET_ID = 42L;
    private static final long AMOUNT = 500L;

    @Autowired
    private MockMvcTester mockMvc;

    @MockitoBean
    private SendMoneyUseCase sendMoneyUseCase;

    private MvcTestResult 송금(Object sourceAccountId, Object targetAccountId, Object amount) {
        return mockMvc.post().uri(PATH, sourceAccountId, targetAccountId, amount).exchange();
    }

    @Nested
    class 송금_요청 {

        @Test
        void 요청이_성공하면_200을_반환한다() {
            MvcTestResult result = 송금(SOURCE_ID, TARGET_ID, AMOUNT);

            assertThat(result).hasStatus(HttpStatus.OK);
        }

        @Test
        void 응답_본문은_비어_있다() {
            MvcTestResult result = 송금(SOURCE_ID, TARGET_ID, AMOUNT);

            assertThat(result).bodyText().isEmpty();
        }

        @Test
        void 유스케이스를_한_번_호출한다() {
            송금(SOURCE_ID, TARGET_ID, AMOUNT);

            then(sendMoneyUseCase).should().sendMoney(any());
        }

        @Test
        void 경로_변수를_담은_커맨드를_유스케이스에_전달한다() {
            ArgumentCaptor<SendMoneyCommand> captor = ArgumentCaptor.forClass(SendMoneyCommand.class);

            송금(SOURCE_ID, TARGET_ID, AMOUNT);

            then(sendMoneyUseCase).should().sendMoney(captor.capture());
            assertThat(captor.getValue()).isEqualTo(new SendMoneyCommand(
                    new AccountId(SOURCE_ID),
                    new AccountId(TARGET_ID),
                    Money.of(AMOUNT)));
        }
    }

    @Nested
    class 잘못된_요청 {

        @Test
        void 계좌ID가_숫자가_아니면_400을_반환한다() {
            MvcTestResult result = 송금("abc", TARGET_ID, AMOUNT);

            assertThat(result).hasStatus(HttpStatus.BAD_REQUEST);
        }

        @Test
        void 금액이_숫자가_아니면_400을_반환한다() {
            MvcTestResult result = 송금(SOURCE_ID, TARGET_ID, "abc");

            assertThat(result).hasStatus(HttpStatus.BAD_REQUEST);
        }

        @Test
        void 경로_변수를_바인딩하지_못하면_유스케이스를_호출하지_않는다() {
            송금(SOURCE_ID, TARGET_ID, "abc");

            then(sendMoneyUseCase).shouldHaveNoInteractions();
        }

        @Test
        void GET으로_요청하면_405를_반환한다() {
            MvcTestResult result = mockMvc.get().uri(PATH, SOURCE_ID, TARGET_ID, AMOUNT).exchange();

            assertThat(result).hasStatus(HttpStatus.METHOD_NOT_ALLOWED);
        }
    }

    @Nested
    class 커맨드_검증 {

        @Test
        void 금액이_0이면_커맨드_검증_예외가_전파된다() {
            MvcTestResult result = 송금(SOURCE_ID, TARGET_ID, 0L);

            assertThat(result).hasFailed();
            assertThat(result).failure()
                    .rootCause()
                    .isInstanceOf(ConstraintViolationException.class)
                    .hasMessageContaining("money");
        }

        @Test
        void 금액이_음수면_커맨드_검증_예외가_전파된다() {
            MvcTestResult result = 송금(SOURCE_ID, TARGET_ID, -1L);

            assertThat(result).hasFailed();
            assertThat(result).failure()
                    .rootCause()
                    .isInstanceOf(ConstraintViolationException.class)
                    .hasMessageContaining("money");
        }

        @Test
        void 커맨드_검증에_걸리면_유스케이스를_호출하지_않는다() {
            송금(SOURCE_ID, TARGET_ID, 0L);

            then(sendMoneyUseCase).shouldHaveNoInteractions();
        }
    }
}
