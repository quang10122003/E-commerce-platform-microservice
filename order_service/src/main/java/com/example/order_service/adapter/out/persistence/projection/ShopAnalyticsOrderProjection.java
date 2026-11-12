package com.example.order_service.adapter.out.persistence.projection;

import com.example.order_service.domain.model.CheckoutPaymentMethod;
import com.example.order_service.domain.model.OrderCancelReason;
import com.example.order_service.domain.model.OrderCancelledBy;
import com.example.order_service.domain.model.OrderStatus;
import com.example.order_service.domain.model.PriceType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface ShopAnalyticsOrderProjection {
    OrderStatus getStatus();
    CheckoutPaymentMethod getPaymentMethod();
    PriceType getPriceType();
    BigDecimal getTotalAmount();
    OrderCancelledBy getCancelledBy();
    OrderCancelReason getCancelReason();
    LocalDateTime getCreatedAt();
    LocalDateTime getConfirmedAt();
    LocalDateTime getShippedAt();
    LocalDateTime getDeliveredAt();
    LocalDateTime getCancelledAt();
    LocalDateTime getPaidAt();
    Long getBuyerId();
}
