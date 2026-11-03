package com.example.auth_service.adapter.out.persistence.outbox;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.domain.Pageable;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.auth_service.adapter.entity.OutboxEventEntity;

public interface OutboxEventRepoJpaJpa extends JpaRepository<OutboxEventEntity, Long> {

    List<OutboxEventEntity> findByStatusOrderByIdAsc(
            OutboxEventEntity.Status status, Pageable pageable);

    @Query(value = "SELECT * FROM outbox_event WHERE event_id = :eventId AND status = 'PENDING' FOR UPDATE SKIP LOCKED", nativeQuery = true)
    Optional<OutboxEventEntity> lockPendingEvent(@Param("eventId") String eventId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from OutboxEventEntity e where e.eventId = :eventId")
    Optional<OutboxEventEntity> findByEventIdForUpdate(@Param("eventId") String eventId);
}
