package com.example.order_service.application.port.out.repo;

import com.example.order_service.domain.model.Checkout;

import java.util.Optional;

public interface CheckoutRepoPort {
    Optional<Checkout> findById(Long checkoutId);
    Optional<Checkout> findByIdForUpdate(Long checkoutId);
    Optional<Checkout> findByIdempotencyKey(String idempotencyKey);
    Checkout save(Checkout checkout);
}
