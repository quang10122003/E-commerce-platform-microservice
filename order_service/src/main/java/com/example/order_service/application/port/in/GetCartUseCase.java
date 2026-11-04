package com.example.order_service.application.port.in;

import com.example.order_service.application.dto.response.CartResponse;

public interface GetCartUseCase {
    CartResponse getCart();
}
