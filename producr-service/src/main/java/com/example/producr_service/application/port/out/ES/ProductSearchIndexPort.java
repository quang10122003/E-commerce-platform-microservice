package com.example.producr_service.application.port.out.ES;

import com.example.producr_service.application.dto.request.ProductScrollFilter;
import com.example.producr_service.application.dto.response.ProductCatalogSearchResponse;
import com.example.producr_service.domain.model.Product;

public interface ProductSearchIndexPort {
    void index(Product product, String location);

    void deleteById(Long productId);

    ProductCatalogSearchResponse search(ProductScrollFilter filter);
}
