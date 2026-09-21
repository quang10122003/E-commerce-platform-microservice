package com.example.producr_service.adapter.out.elasticsearch;

import com.example.producr_service.adapter.DTO.documentElasticsearch.ProductSearchDocument;
import com.example.producr_service.adapter.mapper.ProductSearchDocumentMapper;
import com.example.producr_service.application.port.out.ProductSearchIndexPort;
import com.example.producr_service.domain.model.Product;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true,level = AccessLevel.PRIVATE)
public class ProductElasticsearchAdapter implements ProductSearchIndexPort {

    // Chuyển aggregate Product sang document dành cho Elasticsearch.
    ProductSearchDocumentMapper productSearchDocumentMapper;

    // Thực hiện thao tác lưu document vào Elasticsearch.
    ProductSearchRepository productSearchRepository;

    // Index Product theo ID MySQL để hỗ trợ tạo mới hoặc ghi đè document.
    @Override
    public void index(Product product) {
        ProductSearchDocument document = productSearchDocumentMapper.toDocument(product);
        productSearchRepository.save(document);
    }
}
