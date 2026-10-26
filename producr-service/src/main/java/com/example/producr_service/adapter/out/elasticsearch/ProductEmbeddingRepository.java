package com.example.producr_service.adapter.out.elasticsearch;

import com.example.producr_service.adapter.DTO.documentElasticsearch.ProductEmbeddingDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface ProductEmbeddingRepository extends ElasticsearchRepository<ProductEmbeddingDocument, Long> {
}
