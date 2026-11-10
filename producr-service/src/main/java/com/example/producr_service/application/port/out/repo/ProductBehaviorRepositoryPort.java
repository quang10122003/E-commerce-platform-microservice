package com.example.producr_service.application.port.out.repo;

import com.example.producr_service.domain.model.BehaviorType;
import com.example.producr_service.domain.model.UserProductBehavior;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ProductBehaviorRepositoryPort {
    Optional<Instant> findLatestProductViewAt(Long userId, Long productId);

    boolean saveIfAbsent(UserProductBehavior behavior);

    int deleteExpiredBefore(Instant cutoff, int batchSize);

    // lấy hành vi người dùng trong 30 ngày gần nhất
    List<UserProductBehavior> findRecentByUserId(
            Long userId,
            Instant now);

    // lấy id sản phẩm user đã có hành vi theo loại kể từ thời điểm since
    Set<Long> findProductIdsByUserIdAndTypeSince(Long userId, BehaviorType eventType, Instant since);
}
