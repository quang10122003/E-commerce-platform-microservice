package com.example.producr_service.application.port.out.repo;

import com.example.producr_service.domain.model.ProductViewState;

import java.time.Instant;

public interface ProductViewStateRepositoryPort {
    ProductViewState lockOrCreate(Long userId, Long productId, Instant initialAllowedAt);

    void save(ProductViewState state);
}
