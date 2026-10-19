package com.example.producr_service.adapter.out.elasticsearch;

import com.example.producr_service.adapter.DTO.documentElasticsearch.ProductSearchDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface ProductSearchRepository extends ElasticsearchRepository<ProductSearchDocument, Long> {

}
