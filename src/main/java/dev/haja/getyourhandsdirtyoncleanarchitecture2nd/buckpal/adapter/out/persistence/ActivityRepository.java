package dev.haja.getyourhandsdirtyoncleanarchitecture2nd.buckpal.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

interface ActivityRepository
        extends JpaRepository<ActivityJpaEntity, Long> {
    @Query("""
			select a from ActivityJpaEntity a 
			where a.ownerAccountId = :ownerAccountId 
			and a.timestamp >= :since
			""")
    List<ActivityJpaEntity> findByOwnerSince(
            @Param("ownerAccountId") long ownerAccountId,
            @Param("since") LocalDateTime since);
}
