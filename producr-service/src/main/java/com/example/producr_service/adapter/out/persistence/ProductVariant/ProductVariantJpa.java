package com.example.producr_service.adapter.out.persistence.ProductVariant;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import jakarta.persistence.LockModeType;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.producr_service.adapter.entity.ProductVariantEntity;
import com.example.producr_service.adapter.out.persistence.projection.VariantPriceProjection;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductVariantJpa
        extends JpaRepository<ProductVariantEntity, Long> {

    @Query("""
            select v.id as variantId, v.price as price,
                v.stockQuantity as stockQuantity, p.status as productStatus
            from ProductVariantEntity v join v.product p
            where p.id = :productId and v.id = :variantId
            """)
    Optional<VariantPriceProjection> findPriceView(@Param("productId") Long productId,
                                           @Param("variantId") Long variantId);

    @EntityGraph(attributePaths = {"product", "attributeValues", "attributeValues.attribute"})
    Optional<ProductVariantEntity> findSnapshotById(Long id);

    boolean existsBySku(String sku);
    boolean existsBySkuAndIdNot(String sku, Long variantId);

    // Tải phân loại theo lô để tránh N+1 khi dựng trang sản phẩm.
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
