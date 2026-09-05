package dev.haja.getyourhandsdirtyoncleanarchitecture2nd.buckpal.adapter.out.persistence;

import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.buckpal.application.domain.model.Account;
import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.buckpal.application.domain.model.Account.AccountId;

import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.buckpal.application.port.out.LoadAccountPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;


@Component
@RequiredArgsConstructor
class AccountPersistenceAdapter implements
        LoadAccountPort {
    private final AccountRepository accountRepository;
    private final ActivityRepository activityRepository;

    @Override
    public Account loadAccount(
            AccountId accountId,
            LocalDateTime baselineDate) {
        return null;
    }
}
