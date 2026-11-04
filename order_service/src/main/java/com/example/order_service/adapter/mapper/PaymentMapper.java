package com.example.order_service.adapter.mapper;

import com.example.order_service.adapter.entity.PaymentEntity;
import com.example.order_service.domain.model.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

    @Mapping(target = "checkout", ignore = true)
    PaymentEntity toEntity(Payment payment);

    default Payment toDomain(PaymentEntity entity) {
        return Payment.reconstitute(entity.getId(), entity.getCheckout().getId(),
                entity.getPaymentMethod(), entity.getStatus(), entity.getAmount(), entity.getCurrency(),
                entity.getTransactionReference(), entity.getProviderTransactionId(), entity.getBankCode(),
                entity.getBankTransactionId(), entity.getResponseCode(), entity.getTransactionStatus(),
                entity.getPaymentUrl(), entity.getExpiresAt(), entity.getPaidAt(), entity.getCancelledAt(),
                entity.getCancellationReason(), entity.getCreatedAt(), entity.getUpdatedAt());
    }
}
