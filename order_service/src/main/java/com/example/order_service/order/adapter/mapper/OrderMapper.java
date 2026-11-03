package com.example.order_service.order.adapter.mapper;

import com.example.order_service.order.adapter.entity.OrderEntity;
import com.example.order_service.order.adapter.entity.OrderItemEntity;
import com.example.order_service.order.domain.model.Order;
import com.example.order_service.order.domain.model.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderMapper {
    @Mapping(target = "checkout", ignore = true)
    @Mapping(target = "items", ignore = true)
    OrderEntity toEntity(Order order);

    @Mapping(target = "order", ignore = true)
    OrderItemEntity toEntity(OrderItem item);
}
