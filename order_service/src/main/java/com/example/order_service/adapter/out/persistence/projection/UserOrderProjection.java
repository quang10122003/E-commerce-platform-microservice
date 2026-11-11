package com.example.order_service.adapter.out.persistence.projection;

import com.example.order_service.domain.model.CheckoutPaymentMethod;
import com.example.order_service.domain.model.OrderCancelledBy;
import com.example.order_service.domain.model.OrderCancelReason;
import com.example.order_service.domain.model.OrderStatus;
import com.example.order_service.domain.model.PaymentStatus;
import com.example.order_service.domain.model.PriceType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface UserOrderProjection {
    Long getOrderId();
    Long getCheckoutId();
    OrderStatus getStatus();
    Long getShopId();
    String getShopName();
    String getShopLogoUrl();
    PriceType getPriceType();
    CheckoutPaymentMethod getPaymentMethod();
    BigDecimal getTotalAmount();
    OrderCancelledBy getCancelledBy();    OrderCancelReason getCancelReason();
    LocalDateTime getCancelledAt();
    LocalDateTime getConfirmedAt();
    LocalDateTime getShippedAt();
    LocalDateTime getDeliveredAt();
    LocalDateTime getCreatedAt();
    PaymentStatus getPaymentStatus();
    String getPaymentUrl();
    LocalDateTime getPaymentExpiresAt();
    LocalDateTime getPaymentPaidAt();
}
