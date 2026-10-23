package com.example.producr_service.adapter.out.persistence.behavior;

import com.example.producr_service.adapter.entity.UserProductBehaviorEntity;
import com.example.producr_service.domain.model.BehaviorType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface ProductBehaviorJpa extends JpaRepository<UserProductBehaviorEntity, Long> {
    @Query("""
            select max(b.createdAt) from UserProductBehaviorEntity b
            where b.userId = :userId and b.product.id = :productId and b.eventType = :eventType
            """)
    Optional<LocalDateTime> findLatestCreatedAt(@Param("userId") Long userId,
                                              @Param("productId") Long productId,
                                              @Param("eventType") BehaviorType eventType);

    // Cộng 0 giữ nguyên ID cũ; LAST_INSERT_ID(0) phân biệt retry mà không dựa vào cấu hình affected rows.
    @Modifying
    @Query(value = """
            INSERT INTO user_product_behavior (event_id, user_id, product_id, event_type, created_at)
            VALUES (:eventId, :userId, :productId, :eventType, :createdAt)
            ON DUPLICATE KEY UPDATE id = id + LAST_INSERT_ID(0)
            """, nativeQuery = true)
    void insertIfAbsent(@Param("eventId") String eventId,
                        @Param("userId") Long userId,
                        @Param("productId") Long productId,
                        @Param("eventType") String eventType,
                        @Param("createdAt") LocalDateTime createdAt);

    // Phải đọc ngay trên cùng connection của transaction; bản ghi trùng đặt giá trị này về 0.
    @Query(value = "SELECT LAST_INSERT_ID()", nativeQuery = true)
    Long lastInsertedId();
}
