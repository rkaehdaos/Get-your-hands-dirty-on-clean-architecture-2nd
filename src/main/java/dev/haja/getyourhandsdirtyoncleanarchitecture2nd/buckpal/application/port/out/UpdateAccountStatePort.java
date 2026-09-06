package dev.haja.getyourhandsdirtyoncleanarchitecture2nd.buckpal.application.port.out;

import dev.haja.getyourhandsdirtyoncleanarchitecture2nd.buckpal.application.domain.model.Account;

public interface UpdateAccountStatePort {
    void updateActivities(Account account);
}
