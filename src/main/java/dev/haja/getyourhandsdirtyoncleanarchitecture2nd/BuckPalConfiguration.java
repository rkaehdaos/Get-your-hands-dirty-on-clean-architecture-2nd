package dev.haja.getyourhandsdirtyoncleanarchitecture2nd;

import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.service.MoneyTransferProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BuckPalConfiguration {
    @Bean
    public MoneyTransferProperties moneyTransferProperties(){
        return new MoneyTransferProperties();
    }
}
