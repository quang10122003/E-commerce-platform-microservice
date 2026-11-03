package com.example.producr_service.adapter.out.persistence.outbox;

import com.example.producr_service.adapter.entity.OutboxEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.domain.Pageable;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OutboxEventJpa extends JpaRepository<OutboxEventEntity, Long> {

    List<OutboxEventEntity> findByStatusAndNextAttemptAtLessThanEqualOrderByIdAsc(
            OutboxEventEntity.Status status,
            LocalDateTime nextAttemptAt, Pageable pageable
    );

    // Bỏ qua event đã bị instance khác khóa trong lúc xử lý.
    @Query(value = """
            SELECT * FROM outbox_event
            WHERE event_id = :eventId
              AND status = 'PENDING'
              AND next_attempt_at <= :now
            FOR UPDATE SKIP LOCKED
            """, nativeQuery = true)
    Optional<OutboxEventEntity> lockPendingEvent(
            @Param("eventId") String eventId,
            @Param("now") LocalDateTime now
    );

    Optional<OutboxEventEntity> findByEventId(String eventId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from OutboxEventEntity e where e.eventId = :eventId")
    Optional<OutboxEventEntity> findByEventIdForUpdate(@Param("eventId") String eventId);
}
