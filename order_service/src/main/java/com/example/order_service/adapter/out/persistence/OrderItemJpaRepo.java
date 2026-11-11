package com.example.order_service.adapter.out.persistence;

import com.example.order_service.adapter.entity.OrderItemEntity;
import com.example.order_service.adapter.out.persistence.projection.UserOrderItemProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
                   i.originalUnitPrice as originalUnitPrice, i.unitPrice as unitPrice, i.totalAmount as totalAmount
            from OrderItemEntity i where i.order.id in :orderIds order by i.id
            """)
    List<UserOrderItemProjection> findUserOrderItems(@Param("orderIds") Collection<Long> orderIds);
}
