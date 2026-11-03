package com.example.order_service.order.application.port.out.repo;

import com.example.order_service.order.domain.model.Payment;

import java.util.Optional;

public interface PaymentRepoPort {
    Optional<Payment> findByCheckoutIdForUpdate(Long checkoutId);
    void save(Payment payment);
}
