package com.example.producr_service.adapter.out.persistence.outbox;

import com.example.producr_service.adapter.entity.OutboxEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

// Repository JPA truy vấn event outbox theo trạng thái và thời điểm retry.
public interface OutboxEventJpa extends JpaRepository<OutboxEventEntity, Long> {

    // Lấy event chờ xử lý đã đến thời điểm thử lại.
    List<OutboxEventEntity> findByStatusAndNextAttemptAtLessThanEqualOrderByCreatedAtAsc(
            OutboxEventEntity.Status status,
            LocalDateTime nextAttemptAt
    );

    // Tìm event theo mã định danh bất biến.
    Optional<OutboxEventEntity> findByEventId(String eventId);
}
