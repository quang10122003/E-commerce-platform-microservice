package com.example.producr_service.application.port.out.repo;

import com.example.producr_service.domain.model.Product;

import java.util.Optional;

public interface ProductRepositoryPort {
    Product save(Product product);
    Optional<Product> findById(Long id);
    boolean existsBySku(String sku);
}
