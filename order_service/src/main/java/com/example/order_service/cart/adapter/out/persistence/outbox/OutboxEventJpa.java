package com.example.order_service.cart.adapter.out.persistence.outbox;

import com.example.order_service.cart.adapter.entity.OutboxEventEntity;
import com.example.order_service.cart.domain.model.OutboxStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OutboxEventJpa extends JpaRepository<OutboxEventEntity, Long> {
    List<OutboxEventEntity> findByStatusAndNextAttemptAtLessThanEqualOrderByIdAsc(
            OutboxStatus status, LocalDateTime nextAttemptAt, Pageable pageable);

    // Bỏ qua dòng đang được worker khác xử lý thay vì chờ khóa toàn bộ lượt quét.
    @Query(value = """
            SELECT * FROM outbox_event
            WHERE event_id = :eventId AND status = 'PENDING' AND next_attempt_at <= :now
            FOR UPDATE SKIP LOCKED
            """, nativeQuery = true)
    Optional<OutboxEventEntity> lockPendingEvent(@Param("eventId") String eventId,
                                                @Param("now") LocalDateTime now);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from OutboxEventEntity e where e.eventId = :eventId")
    Optional<OutboxEventEntity> findByEventIdForUpdate(@Param("eventId") String eventId);
}
