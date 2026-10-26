package com.example.producr_service.application.dto.embedding;

import java.util.List;

// DTO Chưa data khi gọi model data lấy vector
public record ProductEmbeddingData(  Long productId,
                                     List<Float> vector,
                                     String contentHash) {
    public ProductEmbeddingData {
        vector = List.copyOf(vector);
    }
}
