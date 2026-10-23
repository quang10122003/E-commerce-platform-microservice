package com.example.producr_service.adapter.out.persistence.behavior;

import com.example.producr_service.adapter.entity.UserProductBehaviorEntity;
import com.example.producr_service.adapter.mapper.ProductBehaviorMapper;
import com.example.producr_service.adapter.out.persistence.Product.ProductJpa;
import com.example.producr_service.domain.model.UserProductBehavior;
import com.example.producr_service.application.port.out.repo.ProductBehaviorRepositoryPort;
import com.example.producr_service.domain.model.BehaviorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ProductBehaviorRepoAdapter implements ProductBehaviorRepositoryPort {
    private final ProductBehaviorJpa productBehaviorJpa;
    private final ProductJpa productJpa;
    private final ProductBehaviorMapper productBehaviorMapper;
    private final Clock clock;

    @Override
    public Optional<Instant> findLatestProductViewAt(Long userId, Long productId) {
        return productBehaviorJpa.findLatestCreatedAt(
                userId, productId, BehaviorType.PRODUCT_VIEW)
                .map(createdAt -> createdAt.atZone(clock.getZone()).toInstant());
    }

    @Override
    public boolean saveIfAbsent(UserProductBehavior behavior) {
        UserProductBehaviorEntity entity = productBehaviorMapper.toEntity(
                behavior, productJpa.getReferenceById(behavior.getProductId()), clock.getZone());
        productBehaviorJpa.insertIfAbsent(entity.getEventId(), entity.getUserId(),
                entity.getProduct().getId(), entity.getEventType().name(), entity.getCreatedAt());
        return productBehaviorJpa.lastInsertedId() > 0;
    }
}
