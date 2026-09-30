package com.example.producr_service.adapter.out.persistence.ProductVariant;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.producr_service.adapter.entity.ProductVariantEntity;
import com.example.producr_service.adapter.entity.VariantImageEntity;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductVariantJpa
        extends JpaRepository<ProductVariantEntity, Long> {

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

    @Query("select variantImageEntity from VariantImageEntity variantImageEntity where variantImageEntity.variant.id in :variantIds")
    List<VariantImageEntity> findVariantImagesByVariantIds(@Param("variantIds") Collection<Long> variantIds);

}
