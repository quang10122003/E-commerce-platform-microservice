package com.example.order_service.cart.application.port.out.repo;

import com.example.order_service.cart.domain.model.Cart;

import java.util.Optional;

public interface CartRepoPort {
    Optional<Cart> findByUserId(Long userId);

    Cart save(Cart cart);
}
