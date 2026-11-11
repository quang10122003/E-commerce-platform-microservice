package com.example.order_service.adapter.out.persistence;

import com.example.order_service.adapter.mapper.CheckoutMapper;
import com.example.common.exception.BusinessException;
import com.example.order_service.application.error.OrderError;
import com.example.order_service.application.port.out.repo.CheckoutRepoPort;
import com.example.order_service.domain.model.Checkout;
import lombok.RequiredArgsConstructor;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CheckoutRepoAdapter implements CheckoutRepoPort {

    private final CheckoutJpaRepo checkoutJpaRepo;
    private final CheckoutMapper checkoutMapper;

    @Override
    public Optional<Checkout> findById(Long checkoutId) {
        return checkoutJpaRepo.findById(checkoutId).map(checkoutMapper::toDomain);
    }

    @Override
    public Optional<Checkout> findByIdForUpdate(Long checkoutId) {
        return checkoutJpaRepo.findByIdForUpdate(checkoutId).map(checkoutMapper::toDomain);
    }

    @Override
    public Optional<Checkout> findByIdempotencyKey(String idempotencyKey) {
        return checkoutJpaRepo.findByIdempotencyKey(idempotencyKey).map(checkoutMapper::toDomain);
    }

    @Override
    public Checkout save(Checkout checkout) {
        try {
            return checkoutMapper.toDomain(checkoutJpaRepo.saveAndFlush(checkoutMapper.toEntity(checkout)));
        } catch (DataIntegrityViolationException exception) {
            Throwable cause = exception;
            while (cause != null) {
                if (cause instanceof ConstraintViolationException violation
                        && violation.getConstraintName() != null
                        && violation.getConstraintName().endsWith("uk_checkout_idempotency_key")) {
                    throw new BusinessException(OrderError.CHECKOUT_IDEMPOTENCY_CONFLICT, exception);
                }
                cause = cause.getCause();
            }
            throw exception;
        }
    }

    @Override
    public boolean existsProcessingByUserId(Long userId) {
        return checkoutJpaRepo.existsByUserIdAndStatus(userId, com.example.order_service.domain.model.CheckoutStatus.PROCESSING);
    }
}
