package com.example.order_service.application.dto.order;

import com.example.order_service.domain.model.CheckoutPaymentMethod;
import com.example.order_service.domain.model.OrderStatus;

import java.time.LocalDateTime;

// Điều kiện truy vấn đã chuẩn hóa: thời gian theo múi giờ của DB, orderId khi từ khóa là số.
public record ShopOrderSearch(Long shopId, OrderStatus status, CheckoutPaymentMethod paymentMethod, String keyword,
                              Long orderId, LocalDateTime from, LocalDateTime to) {
}
