package com.example.order_service.application.dto.order;

import com.example.order_service.domain.model.CheckoutPaymentMethod;
import com.example.order_service.domain.model.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RecentOrderView(Long orderId, Long shopId, String shopName, String recipientName, BigDecimal totalAmount,
                              OrderStatus status, CheckoutPaymentMethod paymentMethod, LocalDateTime createdAt) {
}
