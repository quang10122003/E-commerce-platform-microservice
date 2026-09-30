package com.example.auth_service.adapter.out.persistence.outbox;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.auth_service.adapter.entity.OutboxEventEntity;

public interface OutboxEventRepoJpaJpa extends JpaRepository<OutboxEventEntity, Long> {

    // Lấy event theo thứ tự ID tăng dần.
    List<OutboxEventEntity> findByStatusOrderByIdAsc(
            OutboxEventEntity.Status status);

    @Query(value = "SELECT * FROM outbox_event WHERE event_id = :eventId AND status = 'PENDING' FOR UPDATE SKIP LOCKED", nativeQuery = true)
    Optional<OutboxEventEntity> lockPendingEvent(@Param("eventId") String eventId);

    Optional<OutboxEventEntity> findByEventId(String eventId);
}
