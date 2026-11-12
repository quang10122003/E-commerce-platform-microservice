package com.example.order_service.application.dto.order;

import com.example.order_service.domain.model.CheckoutPaymentMethod;
import com.example.order_service.domain.model.OrderCancelReason;
import com.example.order_service.domain.model.OrderCancelledBy;
import com.example.order_service.domain.model.OrderStatus;
import com.example.order_service.domain.model.PriceType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// Dòng đơn rút gọn cho thống kê; paidAt khác null nghĩa là thanh toán online đã thành công.
public record ShopAnalyticsOrderView(OrderStatus status, CheckoutPaymentMethod paymentMethod, PriceType priceType,
                                     BigDecimal totalAmount, OrderCancelledBy cancelledBy, OrderCancelReason cancelReason,
                                     LocalDateTime createdAt, LocalDateTime confirmedAt, LocalDateTime shippedAt,
                                     LocalDateTime deliveredAt, LocalDateTime cancelledAt, LocalDateTime paidAt,
                                     Long buyerId) {
}
