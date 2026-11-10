package com.example.order_service.application.port.out.repo;

import com.example.order_service.domain.model.Payment;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PaymentRepoPort {
    Optional<Payment> findByCheckoutId(Long checkoutId);
    Optional<Payment> findByCheckoutIdForUpdate(Long checkoutId);
    Optional<Payment> findByTransactionReferenceForUpdate(String transactionReference);
    List<String> findPendingReferencesExpiredBefore(LocalDateTime cutoff, int limit);
    void save(Payment payment);
}
