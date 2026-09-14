package com.example.producr_service.adapter.out.persistence.ProductVariant;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.producr_service.adapter.entity.ProductVariantEntity;

public interface ProductVariantJpa
        extends JpaRepository<ProductVariantEntity, Long> {

    boolean existsBySku(String sku);
}