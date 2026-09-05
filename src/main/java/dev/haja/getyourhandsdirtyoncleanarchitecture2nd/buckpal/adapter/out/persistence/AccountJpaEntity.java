package dev.haja.getyourhandsdirtyoncleanarchitecture2nd.buckpal.adapter.out.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "account")
@EqualsAndHashCode(of = {"id"})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class AccountJpaEntity {
    @Id @GeneratedValue
    private Long id;
}
