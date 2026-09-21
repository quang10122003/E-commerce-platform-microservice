package com.example.producr_service.application.port.out;

import com.example.producr_service.domain.model.Product;

public interface ProductSearchIndexPort {
    // Ghi mới hoặc cập nhật document Product trong search engine.
    void index(Product product);
}
