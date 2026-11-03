package com.example.order_service.order.adapter.out.persistence;

import com.example.order_service.order.adapter.entity.PaymentEntity;
import com.example.order_service.order.adapter.mapper.PaymentMapper;
import com.example.order_service.order.application.port.out.repo.PaymentRepoPort;
import com.example.order_service.order.domain.model.Payment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PaymentRepoAdapter implements PaymentRepoPort {

    private final PaymentJpaRepo paymentJpaRepo;
    private final CheckoutJpaRepo checkoutJpaRepo;
    private final PaymentMapper paymentMapper;

    @Override
    public Optional<Payment> findByCheckoutIdForUpdate(Long checkoutId) {
        return paymentJpaRepo.findByCheckoutIdForUpdate(checkoutId).map(paymentMapper::toDomain);
    }

    @Override
    public void save(Payment payment) {
        PaymentEntity entity = paymentMapper.toEntity(payment);
        entity.setCheckout(checkoutJpaRepo.getReferenceById(payment.getCheckoutId()));
        paymentJpaRepo.save(entity);
    }
}
