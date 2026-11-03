package com.example.order_service.order.adapter.out.persistence.inbox;

import com.example.order_service.order.adapter.entity.InboxEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface InboxEventJpa extends JpaRepository<InboxEventEntity, Long> {
    // LAST_INSERT_ID(0) phân biệt event trùng mà không phụ thuộc cấu hình affected rows.
    @Modifying
    @Query(value = """
            INSERT INTO inbox_events (event_id, event_type, payload, processed_at)
            VALUES (:eventId, :eventType, :payload, :processedAt)
            ON DUPLICATE KEY UPDATE id = id + LAST_INSERT_ID(0)
            """, nativeQuery = true)
    void insertIfAbsent(@Param("eventId") String eventId,
                        @Param("eventType") String eventType,
                        @Param("payload") String payload,
                        @Param("processedAt") LocalDateTime processedAt);

    // Phải đọc ngay sau insert trên cùng connection của transaction.
    @Query(value = "SELECT LAST_INSERT_ID()", nativeQuery = true)
    Long lastInsertedId();
}
