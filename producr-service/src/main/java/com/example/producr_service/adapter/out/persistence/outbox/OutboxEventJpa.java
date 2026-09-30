package com.example.producr_service.adapter.out.persistence.outbox;

import com.example.producr_service.adapter.entity.OutboxEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

// Repository JPA truy vấn event outbox theo trạng thái và thời điểm retry.
public interface OutboxEventJpa extends JpaRepository<OutboxEventEntity, Long> {

    // Lấy event chờ xử lý đã đến thời điểm thử lại.
    List<OutboxEventEntity> findByStatusAndNextAttemptAtLessThanEqualOrderByIdAsc(
            OutboxEventEntity.Status status,
            LocalDateTime nextAttemptAt
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

    // Tìm event theo mã định danh bất biến.
    Optional<OutboxEventEntity> findByEventId(String eventId);
}
