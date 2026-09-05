package dev.haja.getyourhandsdirtyoncleanarchitecture2nd.buckpal.application.port.out;

import java.time.LocalDateTime;

import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.buckpal.application.domain.model.Account;
import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.buckpal.application.domain.model.Account.AccountId;

public interface LoadAccountPort {
    Account loadAccount(AccountId accountId, LocalDateTime baselineDate);
}
