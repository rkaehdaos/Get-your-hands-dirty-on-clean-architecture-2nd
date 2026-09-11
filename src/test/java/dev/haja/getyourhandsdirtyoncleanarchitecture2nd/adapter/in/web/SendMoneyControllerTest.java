package dev.haja.getyourhandsdirtyoncleanarchitecture2nd.adapter.in.web;

import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.port.in.SendMoneyUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = SendMoneyController.class)
class SendMoneyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SendMoneyUseCase sendMoneyUseCase;

    @Test
    void testSendMoney() {

    }
}