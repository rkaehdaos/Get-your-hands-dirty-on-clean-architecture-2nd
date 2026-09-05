package dev.haja.getyourhandsdirtyoncleanarchitecture2nd.buckpal.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

interface ActivityRepository
        extends JpaRepository<ActivityJpaEntity, Long> {
}
