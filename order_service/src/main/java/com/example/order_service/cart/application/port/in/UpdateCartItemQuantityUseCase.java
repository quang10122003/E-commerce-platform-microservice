package com.example.order_service.cart.application.port.in;

public interface UpdateCartItemQuantityUseCase {
    void updateCartItemQuantity(Long productVariantId, int quantity);
}
