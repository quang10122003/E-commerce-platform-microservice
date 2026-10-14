package com.example.order_service.cart.application.dto.request;

import jakarta.validation.constraints.Positive;

public record AddCartItemRequest(
        @Positive(message = "Số lượng phải lớn hơn 0") Integer quantity) {
}
