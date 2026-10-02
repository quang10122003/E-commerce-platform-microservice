package com.example.order_service.cart.application.port.in;

import com.example.order_service.cart.application.dto.repone.CartRepone;

public interface GetCartUseCase {
    CartRepone getCart();
}
