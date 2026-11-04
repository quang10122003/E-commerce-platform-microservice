package com.example.order_service.application.port.in;

public interface UpdateCartItemQuantityUseCase {
    void updateCartItemQuantity(Long productVariantId, int quantity);
}
