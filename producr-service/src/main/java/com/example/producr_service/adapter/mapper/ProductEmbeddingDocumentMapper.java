package com.example.producr_service.adapter.mapper;

import com.example.producr_service.adapter.DTO.documentElasticsearch.ProductEmbeddingDocument;
import com.example.producr_service.application.dto.embedding.ProductEmbeddingData;
import org.springframework.stereotype.Component;

@Component
public class ProductEmbeddingDocumentMapper {
    public ProductEmbeddingDocument toDocument(ProductEmbeddingData embedding) {
        return ProductEmbeddingDocument.builder()
                .productId(embedding.productId())
                .vector(embedding.vector())
                .contentHash(embedding.contentHash())
                .build();
    }

    public ProductEmbeddingData toData(
            ProductEmbeddingDocument document) {

        if (document.getProductId() == null
                || document.getVector() == null
                || document.getVector().isEmpty()) {

            throw new IllegalStateException(
                    "Document embedding thiếu productId hoặc vector");
        }

        return new ProductEmbeddingData(
                document.getProductId(),
                document.getVector(),
                document.getContentHash());
    }
}
