package com.example.order_service.adapter.out.persistence.review;

import com.example.order_service.adapter.entity.ProductReviewEntity;
import com.example.order_service.adapter.out.persistence.projection.RatingCountProjection;
import com.example.order_service.adapter.out.persistence.projection.RatingSummaryProjection;
import com.example.order_service.adapter.out.persistence.projection.ReviewableOrderItemProjection;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ProductReviewJpa extends JpaRepository<ProductReviewEntity, Long> {
    // Người mua và tên hiển thị lấy từ snapshot checkout, không gọi Auth Service.
    @Query("""
            select i.id as orderItemId, o.id as orderId, i.productId as productId, o.shopId as shopId,
                   c.userId as buyerId, c.buyerFullNameSnapshot as buyerName, i.variantAttributes as variantAttributes,
                   o.status as orderStatus, o.deliveredAt as deliveredAt
            from OrderItemEntity i join i.order o join o.checkout c
            where i.id = :orderItemId
            """)
    Optional<ReviewableOrderItemProjection> findReviewableOrderItem(@Param("orderItemId") Long orderItemId);

    boolean existsByOrderItemId(Long orderItemId);

    @Query("""
            select count(r) as reviewCount, coalesce(sum(r.rating), 0) as ratingSum
            from ProductReviewEntity r where r.productId = :productId
            """)
    RatingSummaryProjection findRatingSummary(@Param("productId") Long productId);

    @Query("""
            select r.rating as rating, count(r) as reviewCount
            from ProductReviewEntity r where r.productId = :productId group by r.rating
            """)
    List<RatingCountProjection> countByRating(@Param("productId") Long productId);

    @Query("""
            select r from ProductReviewEntity r
            where r.productId = :productId and (:rating is null or r.rating = :rating)
            order by r.createdAt desc, r.id desc
            """)
    List<ProductReviewEntity> findProductReviews(@Param("productId") Long productId, @Param("rating") Integer rating,
                                                 Pageable pageable);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update ProductReviewEntity r set r.productId = null where r.productId = :productId")
    int clearProductId(@Param("productId") Long productId);

    @Query("""
            select count(r) as reviewCount, coalesce(sum(r.rating), 0) as ratingSum
            from ProductReviewEntity r where r.shopId = :shopId
            """)
    RatingSummaryProjection findShopRatingSummary(@Param("shopId") Long shopId);

    long countByShopIdAndCreatedAtGreaterThanEqual(Long shopId, LocalDateTime since);
}
