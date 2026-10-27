package com.example.producr_service.adapter.out.persistence.Product;

import com.example.producr_service.adapter.entity.ProductEntity;
import com.example.producr_service.adapter.out.persistence.projection.ProductPreferenceProjection;
import com.example.producr_service.adapter.out.persistence.projection.ProductSummaryProjection;
import com.example.producr_service.adapter.out.persistence.projection.TopSellingProductProjection;
import com.example.producr_service.domain.model.ProductStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ProductJpa extends JpaRepository<ProductEntity,Long> {
    @Query("""
            select p.id from ProductEntity p
            where p.status = :status and p.category.id in :categoryIds
              and exists (select v.id from ProductVariantEntity v
                          where v.product = p and v.stockQuantity > 0)
            order by p.totalSold desc, p.id desc
            """)
    List<Long> findCategoryCandidateIds(@Param("categoryIds") Collection<Long> categoryIds,
                                        @Param("status") ProductStatus status, Pageable pageable);

    @Query("""
            select p.id from ProductEntity p
            where p.status = :status and p.brand.id in :brandIds
              and exists (select v.id from ProductVariantEntity v
                          where v.product = p and v.stockQuantity > 0)
            order by p.totalSold desc, p.id desc
            """)
    List<Long> findBrandCandidateIds(@Param("brandIds") Collection<Long> brandIds,
                                     @Param("status") ProductStatus status, Pageable pageable);

    @Query("""
            select p.id from ProductEntity p
            where p.id in :productIds and p.status = :status
              and exists (select v.id from ProductVariantEntity v
                          where v.product = p and v.stockQuantity > 0)
            """)
    Set<Long> findAvailableProductIds(@Param("productIds") Collection<Long> productIds,
                                     @Param("status") ProductStatus status);

    @Query("""
            select p.id as id, p.name as name, p.locationSnapshot as locationSnapshot,
                   p.userId as userId, p.description as description,
                   c.id as categoryId, c.name as categoryName, b.id as brandId, b.name as brandName,
                   p.status as status, p.imageUrl as imageUrl, max(v.price) as maxPrice,
                   p.totalSold as totalSold
            from ProductEntity p
            join p.category c
            left join p.brand b
            join p.variants v
            where p.id in :productIds and p.status = :status
              and exists (select available.id from ProductVariantEntity available
                          where available.product = p and available.stockQuantity > 0)
            group by p.id, p.name, p.locationSnapshot, p.userId, p.description,
                     c.id, c.name, b.id, b.name, p.status, p.imageUrl, p.totalSold
            """)
    List<TopSellingProductProjection> findAvailableSearchItemsByIds(
            @Param("productIds") Collection<Long> productIds, @Param("status") ProductStatus status);

    @Query("""
            SELECT p.id AS id, p.name AS name, p.locationSnapshot AS locationSnapshot, p.userId AS userId,
                   p.description AS description, c.id AS categoryId, c.name AS categoryName,
                   b.id AS brandId, b.name AS brandName, p.status AS status,
                   p.imageUrl AS imageUrl, MAX(v.price) AS maxPrice, p.totalSold AS totalSold
            FROM ProductEntity p
            JOIN p.category c
            LEFT JOIN p.brand b
            LEFT JOIN p.variants v
            WHERE p.status = :status
            GROUP BY p.id, p.name, p.locationSnapshot, p.userId, p.description, c.id, c.name,
                     b.id, b.name, p.status, p.imageUrl, p.totalSold
            ORDER BY p.totalSold DESC, p.id DESC
            """)
    List<TopSellingProductProjection> findTopSellingProducts(
            @Param("status") ProductStatus status, Pageable pageable);

    long countByUserIdAndStatus(Long userId, ProductStatus status);

    @Query("select p.id as id, p.name as name, p.imageUrl as imageUrl from ProductEntity p where p.id in :productIds")
    List<ProductSummaryProjection> findProductSummariesByIds(@Param("productIds") List<Long> productIds);
    @Query("select p.userId from ProductEntity p where p.id = :productId")
    Optional<Long> findOwnerIdById(@Param("productId") Long productId);

    @EntityGraph(attributePaths = {"category", "brand"})
    List<ProductEntity> findAllByUserId(Long userId);

    // Tải kèm danh mục và thương hiệu để tránh N+1 khi dựng trang sản phẩm.
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

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
SELECT p from ProductEntity p where  p.id = :productId
""")
    Optional<ProductEntity> findByIdForLock(@Param("productId") Long productId);

    @Query("""
            select p.id as productId,
                           c.id as categoryId,
                           b.id as brandId,
                           max(v.price) as price
                    from ProductEntity p
                    join p.category c
                    left join p.brand b
                    left join p.variants v
                    where p.id in :productIds
                    group by p.id, c.id, b.id
            """)
    List<ProductPreferenceProjection> findPreferenceDataByIds(
            @Param("productIds") Collection<Long> productIds);
}
