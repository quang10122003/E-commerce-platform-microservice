package com.example.producr_service.adapter.out.persistence.ProductVariant;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.producr_service.adapter.entity.ProductVariantEntity;

public interface ProductVariantJpa
        extends JpaRepository<ProductVariantEntity, Long> {

    boolean existsBySku(String sku);

    // Tải phân loại và thuộc tính của các sản phẩm trong một trang.
    @EntityGraph(attributePaths = {"attributeValues", "attributeValues.attribute"})
    List<ProductVariantEntity> findByProduct_IdIn(Collection<Long> productIds);
}
