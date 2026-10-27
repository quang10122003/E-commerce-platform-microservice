package com.example.producr_service.application.port.out.embedding;

import com.example.producr_service.application.dto.embedding.ProductEmbeddingData;

import java.util.Collection;
import java.util.Map;
import java.util.List;
import java.util.Optional;

public interface ProductEmbeddingRepositoryPort {
    List<Long> findSimilarProductIds(List<Float> vectorSrc, int limit);

    Optional<String> findContentHashByProductId(Long productId);

    void save(ProductEmbeddingData embedding);

    void deleteById(Long productId);

    Map<Long, ProductEmbeddingData> findAllByProductIds(
            Collection<Long> productIds);
}
