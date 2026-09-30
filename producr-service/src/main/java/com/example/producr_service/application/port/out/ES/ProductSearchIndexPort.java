package com.example.producr_service.application.port.out.ES;

import com.example.producr_service.application.dto.request.ProductScrollFilter;
import com.example.producr_service.application.dto.response.ProductCatalogSearchResponse;
import com.example.producr_service.domain.model.Product;

// Định nghĩa thao tác đồng bộ sản phẩm vào chỉ mục tìm kiếm.
public interface ProductSearchIndexPort {
    // Ghi mới hoặc cập nhật document Product trong search engine.
    void index(Product product, String location);

    void deleteById(Long productId);

    ProductCatalogSearchResponse search(ProductScrollFilter filter);
}
