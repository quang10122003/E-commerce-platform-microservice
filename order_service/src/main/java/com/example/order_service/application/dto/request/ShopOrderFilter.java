package com.example.order_service.application.dto.request;

import com.example.order_service.domain.model.CheckoutPaymentMethod;
import com.example.order_service.domain.model.OrderStatus;

import java.time.OffsetDateTime;

// Bộ lọc danh sách đơn của shop; from/to có offset để BE tự đổi sang múi giờ lưu trong DB.
public record ShopOrderFilter(OrderStatus status, CheckoutPaymentMethod paymentMethod, String keyword,
                              OffsetDateTime from, OffsetDateTime to) {
}
