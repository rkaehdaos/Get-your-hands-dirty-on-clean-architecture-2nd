package dev.haja.getyourhandsdirtyoncleanarchitecture2nd;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.test.context.jdbc.Sql;

import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;

import org.junit.jupiter.api.Test;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@AutoConfigureTestRestTemplate
@SpringBootTest(webEnvironment = RANDOM_PORT)
class SendMoneySystemTest {

    @Autowired private TestRestTemplate testRestTemplate;

    @Test
    @Sql("SendMoneySystemTest.sql")
    void sendMoney() {
    }
}
