package com.example.order_service.application.dto.order;

import com.example.order_service.domain.model.CheckoutPaymentMethod;
import com.example.order_service.domain.model.OrderCancelledBy;
import com.example.order_service.domain.model.OrderCancelReason;
import com.example.order_service.domain.model.OrderStatus;
import com.example.order_service.domain.model.PaymentStatus;
import com.example.order_service.domain.model.PriceType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record UserOrderView(
        Long orderId, Long checkoutId, OrderStatus status, Long shopId, String shopName, String shopLogoUrl,
        PriceType priceType, CheckoutPaymentMethod paymentMethod, BigDecimal totalAmount,
        OrderCancelledBy cancelledBy, OrderCancelReason cancelReason, LocalDateTime cancelledAt,
        LocalDateTime confirmedAt, LocalDateTime shippedAt, LocalDateTime deliveredAt, LocalDateTime createdAt,
        PaymentStatus paymentStatus, String paymentUrl, LocalDateTime paymentExpiresAt, LocalDateTime paymentPaidAt) {
}
