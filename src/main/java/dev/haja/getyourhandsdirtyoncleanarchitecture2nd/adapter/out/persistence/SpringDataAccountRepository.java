package dev.haja.getyourhandsdirtyoncleanarchitecture2nd.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataAccountRepository
        extends JpaRepository<AccountJpaEntity, Long> {}
