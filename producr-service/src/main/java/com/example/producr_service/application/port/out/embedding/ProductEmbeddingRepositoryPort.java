package com.example.producr_service.application.port.out.embedding;

import com.example.producr_service.application.dto.embedding.ProductEmbeddingData;

import java.util.Optional;

public interface ProductEmbeddingRepositoryPort {
    Optional<String> findContentHashByProductId(Long productId);

    void save(ProductEmbeddingData embedding);

    void deleteById(Long productId);
}
