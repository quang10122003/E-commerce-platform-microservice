package com.example.order_service.adapter.mapper;

import com.example.order_service.adapter.entity.OrderEntity;
import com.example.order_service.adapter.entity.OrderItemEntity;
import com.example.order_service.domain.model.Order;
import com.example.order_service.domain.model.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderMapper {
    @Mapping(target = "checkout", ignore = true)
    @Mapping(target = "items", ignore = true)
    OrderEntity toEntity(Order order);

    @Mapping(target = "order", ignore = true)
    OrderItemEntity toEntity(OrderItem item);

    default Order toDomain(OrderEntity entity) {
        return Order.reconstitute(entity.getId(), entity.getCheckout().getId(), entity.getShopId(),
                entity.getShopNameSnapshot(), entity.getShopLogoUrlSnapshot(), entity.getPriceType(),
                entity.getPaymentMethod(), entity.getStatus(), entity.getTotalAmount(), entity.getCancellationReason(),
                entity.getCancelledAt(), entity.getCreatedAt(), entity.getUpdatedAt(),
                entity.getItems().stream().map(this::toDomain).toList());
    }

    default OrderItem toDomain(OrderItemEntity entity) {
        return OrderItem.reconstitute(entity.getId(), entity.getProductId(), entity.getProductVariantId(),
                entity.getProductName(), entity.getVariantAttributes(), entity.getImageUrl(),
                entity.getQuantity(), entity.getOriginalUnitPrice(), entity.getUnitPrice(),
                entity.getTotalAmount(), entity.getPriceType(), entity.getCreatedAt());
    }
}
