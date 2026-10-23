package com.example.producr_service.application.port.out.repo;

import com.example.producr_service.domain.model.UserProductBehavior;

import java.time.Instant;
import java.util.Optional;

public interface ProductBehaviorRepositoryPort {
    Optional<Instant> findLatestProductViewAt(Long userId, Long productId);

    boolean saveIfAbsent(UserProductBehavior behavior);
}
