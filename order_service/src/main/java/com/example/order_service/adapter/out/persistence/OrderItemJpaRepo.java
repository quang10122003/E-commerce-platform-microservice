package com.example.order_service.adapter.out.persistence;

import com.example.order_service.adapter.entity.OrderItemEntity;
import com.example.order_service.adapter.out.persistence.projection.ShopTopProductProjection;
import com.example.order_service.adapter.out.persistence.projection.UserOrderItemProjection;
import com.example.order_service.domain.model.OrderStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface OrderItemJpaRepo extends JpaRepository<OrderItemEntity, Long> {
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update OrderItemEntity i set i.productId = null where i.productId = :productId")
    int clearProductId(@Param("productId") Long productId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update OrderItemEntity i set i.productVariantId = null where i.productVariantId = :variantId")
    int clearProductVariantId(@Param("variantId") Long variantId);


    @Query("""
            select i.order.id as orderId, i.id as orderItemId, i.productId as productId,
                   i.productVariantId as variantId, i.productName as productName,
                   i.variantAttributes as variantAttributes, i.imageUrl as imageUrl, i.quantity as quantity,
                   i.originalUnitPrice as originalUnitPrice, i.unitPrice as unitPrice, i.totalAmount as totalAmount,
                   exists (select 1 from ProductReviewEntity r where r.orderItemId = i.id) as reviewed
            from OrderItemEntity i where i.order.id in :orderIds order by i.id
            """)
    List<UserOrderItemProjection> findUserOrderItems(@Param("orderIds") Collection<Long> orderIds);

    @Query("""
            select i.productId as productId, i.productName as productName, max(i.imageUrl) as imageUrl,
                   sum(i.quantity) as quantity, sum(i.totalAmount) as revenue
            from OrderItemEntity i join i.order o
            where (:shopId is null or o.shopId = :shopId) and o.createdAt between :from and :to and o.status <> :excluded
            group by i.productId, i.productName
            order by sum(i.totalAmount) desc
            """)
    List<ShopTopProductProjection> findShopTopProducts(@Param("shopId") Long shopId, @Param("from") LocalDateTime from,
                                                       @Param("to") LocalDateTime to,
                                                       @Param("excluded") OrderStatus excluded, Pageable pageable);

    @Query("""
            select coalesce(sum((i.originalUnitPrice - i.unitPrice) * i.quantity), 0)
            from OrderItemEntity i join i.order o
            where (:shopId is null or o.shopId = :shopId) and o.createdAt between :from and :to and o.status <> :excluded
            """)
    BigDecimal sumShopSaleDiscount(@Param("shopId") Long shopId, @Param("from") LocalDateTime from,
                                   @Param("to") LocalDateTime to, @Param("excluded") OrderStatus excluded);
}
