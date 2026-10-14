package com.example.order_service.cart.application.port.in;

public interface AddCartItemUseCase {
    void addCartItem(Long productVariantID);

    void addCartItem(Long productVariantId, Integer quantity);
}
