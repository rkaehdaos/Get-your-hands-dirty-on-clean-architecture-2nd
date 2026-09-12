package dev.haja.getyourhandsdirtyoncleanarchitecture2nd.adapter.out.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity @Data
@Table(name = "account")
@EqualsAndHashCode(of = {"id"})
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class AccountJpaEntity {
    @Id @GeneratedValue
    private Long id;
}
