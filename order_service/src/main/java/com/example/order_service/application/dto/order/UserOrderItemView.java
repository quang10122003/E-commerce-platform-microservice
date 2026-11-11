package com.example.order_service.application.dto.order;

import com.example.order_service.application.dto.response.UserOrderResponse;

public record UserOrderItemView(Long orderId, UserOrderResponse.Item item) {
}
