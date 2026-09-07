package dev.haja.getyourhandsdirtyoncleanarchitecture2nd.application.domain.model;

import java.util.Objects;

public class Account {

    /**
     * 식별자가 있는 계좌만 표현한다. "아직 id가 없음"은 AccountId(null)이 아니라
     * AccountId 참조 자체가 null인 것으로만 표현한다.
     * AccountId(null)을 허용하면 서로 다른 미영속 계좌가 equals로 같아진다.
     */
    public record AccountId(Long value) {
        public AccountId {
            Objects.requireNonNull(value, "value must not be null");
        }
    }
}
