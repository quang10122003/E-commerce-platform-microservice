package com.example.producr_service.adapter.out.persistence.behavior;

import com.example.producr_service.application.port.out.repo.ProductViewStateRepositoryPort;
import com.example.producr_service.adapter.entity.UserProductViewStateEntity;
import com.example.producr_service.adapter.mapper.ProductViewStateMapper;
import com.example.producr_service.domain.model.ProductViewState;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;

@Repository
@RequiredArgsConstructor
public class ProductViewStateRepoAdapter implements ProductViewStateRepositoryPort {
    private final ProductViewStateJpa productViewStateJpa;
    private final Clock clock;
    private final ProductViewStateMapper productViewStateMapper;

    @Override
    public ProductViewState lockOrCreate(Long userId, Long productId, Instant initialAllowedAt) {
        productViewStateJpa.createIfAbsent(userId, productId, LocalDateTime.ofInstant(initialAllowedAt, clock.getZone()));
        UserProductViewStateEntity entity = productViewStateJpa.findByUserIdAndProduct_Id(userId, productId)
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy trạng thái lượt xem sau khi tạo"));
        return productViewStateMapper.toDomain(entity, clock.getZone());
    }

    @Override
    public void save(ProductViewState state) {
        productViewStateJpa.updateNextAllowedAt(state.getUserId(), state.getProductId(),
                LocalDateTime.ofInstant(state.getNextAllowedAt(), clock.getZone()));
    }
}
