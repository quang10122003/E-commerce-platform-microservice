package com.example.order_service.adapter.out.persistence;

import com.example.order_service.adapter.entity.PaymentEntity;
import com.example.order_service.adapter.mapper.PaymentMapper;
import com.example.order_service.application.port.out.repo.PaymentRepoPort;
import com.example.order_service.domain.model.Payment;
import com.example.order_service.domain.model.PaymentStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PaymentRepoAdapter implements PaymentRepoPort {

    private final PaymentJpaRepo paymentJpaRepo;
    private final CheckoutJpaRepo checkoutJpaRepo;
    private final PaymentMapper paymentMapper;

    @Override
    public Optional<Payment> findByCheckoutId(Long checkoutId) {
        return paymentJpaRepo.findByCheckoutId(checkoutId).map(paymentMapper::toDomain);
    }

    @Override
    public Optional<Payment> findByCheckoutIdForUpdate(Long checkoutId) {
        return paymentJpaRepo.findByCheckoutIdForUpdate(checkoutId).map(paymentMapper::toDomain);
    }

    @Override
    public Optional<Payment> findByTransactionReferenceForUpdate(String transactionReference) {
        return paymentJpaRepo.findByTransactionReferenceForUpdate(transactionReference)
                .map(paymentMapper::toDomain);
    }

    @Override
    public List<String> findPendingReferencesExpiredBefore(LocalDateTime cutoff, int limit) {
        return paymentJpaRepo.findReferencesByStatusExpiredBefore(PaymentStatus.PENDING, cutoff,
                PageRequest.of(0, limit));
    }

    @Override
    public void save(Payment payment) {
        PaymentEntity entity = paymentMapper.toEntity(payment);
        entity.setCheckout(checkoutJpaRepo.getReferenceById(payment.getCheckoutId()));
        paymentJpaRepo.save(entity);
    }
}
