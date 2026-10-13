package com.example.producr_service.adapter.out.persistence.ProductVariant;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import jakarta.persistence.LockModeType;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.producr_service.adapter.entity.ProductVariantEntity;
import com.example.producr_service.application.dto.response.VariantPriceView;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductVariantJpa
        extends JpaRepository<ProductVariantEntity, Long> {

    // Projection chỉ lấy dữ liệu cần để kiểm tra ưu đãi của variant thuộc product.
    @Query("""
            select new com.example.producr_service.application.dto.response.VariantPriceView(
                v.id, v.price, v.stockQuantity, p.status)
            from ProductVariantEntity v join v.product p
            where p.id = :productId and v.id = :variantId
            """)
    Optional<VariantPriceView> findPriceView(@Param("productId") Long productId,
                                           @Param("variantId") Long variantId);

    // Tải dữ liệu tạo snapshot bằng EntityGraph, không khóa product hoặc variant.
    @EntityGraph(attributePaths = {"product", "attributeValues", "attributeValues.attribute"})
    Optional<ProductVariantEntity> findSnapshotById(Long id);

    boolean existsBySku(String sku);
    boolean existsBySkuAndIdNot(String sku, Long variantId);

    // Tải phân loại và thuộc tính của các sản phẩm trong một trang.
    @EntityGraph(attributePaths = {"attributeValues", "attributeValues.attribute"})
    List<ProductVariantEntity> findByProduct_IdIn(Collection<Long> productIds);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select v from  ProductVariantEntity v where  v.product.id = :productId order by v.id ASC
            """)
    List<ProductVariantEntity> findByIdForLock(@Param("productId") Long productId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select v from ProductVariantEntity v where v.product.id = :productId and v.id = :variantId
            """)
    Optional<ProductVariantEntity> findByProductIdAndIdForUpdate(
            @Param("productId") Long productId,
            @Param("variantId") Long variantId
    );


    long countByProduct_Id(Long productId);

    @EntityGraph(attributePaths = {"attributeValues", "attributeValues.attribute","product"})
    @Override
    List<ProductVariantEntity> findAllById(@NonNull Iterable<Long> longs);
}
