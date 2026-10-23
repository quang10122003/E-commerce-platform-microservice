package com.example.producr_service.adapter.out.persistence.behavior;

import com.example.producr_service.adapter.entity.UserProductViewStateEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface ProductViewStateJpa extends JpaRepository<UserProductViewStateEntity, Long> {
    @Modifying
    @Query(value = """
            INSERT INTO user_product_view_state (user_id, product_id, next_allowed_at)
            VALUES (:userId, :productId, :initialAllowedAt)
            ON DUPLICATE KEY UPDATE id = id
            """, nativeQuery = true)
    void createIfAbsent(@Param("userId") Long userId, @Param("productId") Long productId,
                        @Param("initialAllowedAt") LocalDateTime initialAllowedAt);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<UserProductViewStateEntity> findByUserIdAndProduct_Id(Long userId, Long productId);

    @Modifying
    @Query("""
            update UserProductViewStateEntity s set s.nextAllowedAt = :nextAllowedAt
            where s.userId = :userId and s.product.id = :productId
            """)
    void updateNextAllowedAt(@Param("userId") Long userId, @Param("productId") Long productId,
                             @Param("nextAllowedAt") LocalDateTime nextAllowedAt);
}
