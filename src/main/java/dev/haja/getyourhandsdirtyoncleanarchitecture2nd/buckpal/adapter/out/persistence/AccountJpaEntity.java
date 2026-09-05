package dev.haja.getyourhandsdirtyoncleanarchitecture2nd.buckpal.adapter.out.persistence;

import jakarta.persistence.*;
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
