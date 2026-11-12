package com.example.order_service.adapter.mapper;

import com.example.order_service.adapter.entity.OrderEntity;
import com.example.order_service.adapter.entity.OrderItemEntity;
import com.example.order_service.adapter.out.persistence.projection.UserOrderItemProjection;
import com.example.order_service.adapter.out.persistence.projection.UserOrderProjection;
import com.example.order_service.application.dto.order.UserOrderView;
import com.example.order_service.application.dto.order.ShopOrderView;
import com.example.order_service.application.dto.response.ShopOrderResponse;
import com.example.order_service.adapter.out.persistence.projection.ShopOrderProjection;
import com.example.order_service.adapter.out.persistence.projection.ShopAnalyticsOrderProjection;
import com.example.order_service.adapter.out.persistence.projection.ShopTopProductProjection;
import com.example.order_service.adapter.out.persistence.projection.TopShopProjection;
import com.example.order_service.application.dto.response.AdminAnalyticsResponse;
import com.example.order_service.application.dto.order.ShopAnalyticsOrderView;
import com.example.order_service.application.dto.response.ShopAnalyticsResponse;
import com.example.order_service.application.dto.response.UserOrderResponse;
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
                entity.getPaymentMethod(), entity.getStatus(), entity.getTotalAmount(), entity.getCancelledBy(), entity.getCancelReason(),
                entity.getCancelledAt(), entity.getConfirmedAt(), entity.getShippedAt(), entity.getDeliveredAt(), entity.getCreatedAt(), entity.getUpdatedAt(),
                entity.getItems().stream().map(this::toDomain).toList());
    }

    UserOrderView toUserOrderView(UserOrderProjection order);

    default ShopOrderView toShopOrderView(ShopOrderProjection order) {
        return new ShopOrderView(toUserOrderView(order), new ShopOrderResponse.Buyer(order.getBuyerFullName(),
                order.getBuyerEmail(), order.getShippingAddress(), order.getNote()));
    }

    UserOrderResponse.Item toUserOrderItem(UserOrderItemProjection item);

    ShopAnalyticsOrderView toShopAnalyticsOrderView(ShopAnalyticsOrderProjection order);

    ShopAnalyticsResponse.TopProduct toShopTopProduct(ShopTopProductProjection product);

    AdminAnalyticsResponse.TopShop toTopShop(TopShopProjection shop);

    default OrderItem toDomain(OrderItemEntity entity) {
        return OrderItem.reconstitute(entity.getId(), entity.getProductId(), entity.getProductVariantId(),
                entity.getProductName(), entity.getVariantAttributes(), entity.getImageUrl(),
                entity.getQuantity(), entity.getOriginalUnitPrice(), entity.getUnitPrice(),
                entity.getTotalAmount(), entity.getPriceType(), entity.getCreatedAt());
    }
}
