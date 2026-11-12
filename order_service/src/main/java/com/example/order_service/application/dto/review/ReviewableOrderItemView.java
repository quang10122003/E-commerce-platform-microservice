package com.example.order_service.application.dto.review;

import com.example.order_service.domain.model.OrderStatus;

import java.time.LocalDateTime;

// Món trong đơn kèm trạng thái đơn và người mua để kiểm tra quyền đánh giá; lấy từ order_items, orders và checkout.
public record ReviewableOrderItemView(Long orderItemId, Long orderId, Long productId, Long shopId, Long buyerId,
                                      String buyerName, String variantAttributes, OrderStatus orderStatus,
                                      LocalDateTime deliveredAt) {
}
