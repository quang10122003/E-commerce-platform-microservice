package com.example.producr_service.adapter.out.elasticsearch;

import com.example.producr_service.adapter.DTO.documentElasticsearch.ProductEmbeddingDocument;
import com.example.producr_service.adapter.mapper.ProductEmbeddingDocumentMapper;
import com.example.producr_service.application.constant.Constant;
import com.example.producr_service.application.dto.embedding.ProductEmbeddingData;
import com.example.producr_service.application.port.out.embedding.ProductEmbeddingRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ProductEmbeddingElasticsearchAdapter
        implements ProductEmbeddingRepositoryPort {

    private final ProductEmbeddingRepository repository;
    private final ProductEmbeddingDocumentMapper mapper;

    @Override
    public void deleteById(Long productId) {
        repository.deleteById(productId);
    }

    @Override
    public Optional<String> findContentHashByProductId(Long productId) {
        return repository.findById(productId)
                .map(ProductEmbeddingDocument::getContentHash);
    }

    @Override
    public void save(ProductEmbeddingData embedding) {
        if (embedding.vector().size()
                != Constant.PRODUCT_EMBEDDING_DIMENSIONS) {
            throw new IllegalArgumentException(
                    "Số chiều vector không khớp mapping Elasticsearch");
        }

        ProductEmbeddingDocument document = mapper.toDocument(embedding);
        repository.save(document);
    }
}
