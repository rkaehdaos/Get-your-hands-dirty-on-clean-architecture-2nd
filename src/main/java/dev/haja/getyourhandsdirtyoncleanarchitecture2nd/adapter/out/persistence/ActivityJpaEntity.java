package dev.haja.getyourhandsdirtyoncleanarchitecture2nd.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity @Data
@Table(name = "Activity")
@EqualsAndHashCode(of = {"id"})
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class ActivityJpaEntity {
    @Id @GeneratedValue
    private Long id;

    @Column private LocalDateTime timestamp;
    @Column private Long ownerAccountId;
    @Column private Long sourceAccountId;
    @Column private Long targetAccountId;
    @Column private Long amount;
}
