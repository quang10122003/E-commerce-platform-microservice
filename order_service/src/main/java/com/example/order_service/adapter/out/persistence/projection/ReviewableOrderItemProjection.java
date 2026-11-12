package com.example.order_service.adapter.out.persistence.projection;

import com.example.order_service.domain.model.OrderStatus;

import java.time.LocalDateTime;

public interface ReviewableOrderItemProjection {
    Long getOrderItemId();
    Long getOrderId();
    Long getProductId();
    Long getShopId();
    Long getBuyerId();
    String getBuyerName();
    String getVariantAttributes();
    OrderStatus getOrderStatus();
    LocalDateTime getDeliveredAt();
}
