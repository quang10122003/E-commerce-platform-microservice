package com.example.producr_service.adapter.out.persistence.Product;

import com.example.producr_service.adapter.entity.ProductEntity;
import com.example.producr_service.domain.model.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductJpa extends JpaRepository<ProductEntity,Long> {
    // Tải tên danh mục và thương hiệu cùng trang sản phẩm.
    @EntityGraph(attributePaths = {"category", "brand"})
    @Query(
            value = """
          SELECT p
          FROM ProductEntity p
          WHERE p.userId = :sellerUserId
            AND (:categoryId IS NULL OR p.category.id = :categoryId)
            AND (
                :status IS NULL
                OR (:status = 'INACTIVE' AND p.status = :inactive
                     AND EXISTS (
                        SELECT v.id FROM ProductVariantEntity v
                        WHERE v.product = p AND v.stockQuantity > 0
                    ))
                OR (:status = 'ACTIVE' AND p.status = :active
                    AND EXISTS (
                        SELECT v.id FROM ProductVariantEntity v
                        WHERE v.product = p AND v.stockQuantity > 0
                    ))
                OR (:status = 'OUT_OF_STOCK' AND NOT EXISTS (
                        SELECT v.id FROM ProductVariantEntity v
                        WHERE v.product = p AND v.stockQuantity > 0
                    ))
            )
          AND(:keyword IS NULL OR p.name LIKE %:keyword%)
          ORDER BY p.createdAt DESC, p.id DESC
          """,
            countQuery = """
          SELECT COUNT(p)
          FROM ProductEntity p
          WHERE p.userId = :sellerUserId
            AND (:categoryId IS NULL OR p.category.id = :categoryId)
            AND (
                :status IS NULL
                OR (:status = 'INACTIVE' AND p.status = :inactive
                    AND EXISTS (
                        SELECT v.id FROM ProductVariantEntity v
                        WHERE v.product = p AND v.stockQuantity > 0
                    ))
                OR (:status = 'ACTIVE' AND p.status = :active
                    AND EXISTS (
                        SELECT v.id FROM ProductVariantEntity v
                        WHERE v.product = p AND v.stockQuantity > 0
                    ))
                OR (:status = 'OUT_OF_STOCK' AND NOT EXISTS (
                        SELECT v.id FROM ProductVariantEntity v
                        WHERE v.product = p AND v.stockQuantity > 0
                    ))
            )
            AND(:keyword IS NULL OR p.name LIKE %:keyword%)
          """
    )
    Page<ProductEntity> findSellerProducts(
            @Param("sellerUserId") Long sellerUserId,
            @Param("categoryId") Long categoryId,
            @Param("status") String status,
            @Param("active") ProductStatus active,
            @Param("inactive") ProductStatus inactive,
            @Param("keyword") String keyword,
            Pageable pageable
    );

}
