package com.example.order_service.adapter.mapper;

import com.example.order_service.adapter.entity.CheckoutEntity;
import com.example.order_service.domain.model.Checkout;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CheckoutMapper {

    @Mapping(target = "orders", ignore = true)
    CheckoutEntity toEntity(Checkout checkout);

    default Checkout toDomain(CheckoutEntity entity) {
        return Checkout.reconstitute(entity.getId(), entity.getUserId(),
                entity.getBuyerFullNameSnapshot(), entity.getBuyerEmailSnapshot(),
                entity.getIdempotencyKey(), entity.getShippingAddressSnapshot(), entity.getRecipientNameSnapshot(),
                entity.getPhoneSnapshot(), entity.getClientIp(),
                entity.getNote(), entity.getTotalAmount(), entity.getStatus(), entity.getCancellationReason(),
                entity.getCreatedAt(), entity.getUpdatedAt());
    }
}
