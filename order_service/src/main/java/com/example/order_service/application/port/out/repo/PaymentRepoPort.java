package com.example.order_service.application.port.out.repo;

import com.example.order_service.domain.model.Payment;

import java.util.Optional;

public interface PaymentRepoPort {
    Optional<Payment> findByCheckoutIdForUpdate(Long checkoutId);
    void save(Payment payment);
}
