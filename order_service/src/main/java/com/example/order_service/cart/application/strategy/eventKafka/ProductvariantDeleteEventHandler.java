package com.example.order_service.cart.application.strategy.eventKafka;

import com.example.order_service.cart.application.dto.event.ProductvariantDeleteEvent;
import com.example.order_service.cart.application.port.out.objettMapper.ObjectMapperPort;
import com.example.order_service.cart.application.service.CartService;

public class ProductvariantDeleteEventHandler implements EventHandler {
    private final ObjectMapperPort objectMapperPort;
    private final CartService cartService;

    // Nhận parser và service nghiệp vụ được lắp ghép tại infrastructure.
    public ProductvariantDeleteEventHandler(ObjectMapperPort objectMapperPort, CartService cartService) {
        this.objectMapperPort = objectMapperPort;
        this.cartService = cartService;
    }

    // Khai báo loại event xóa một biến thể.
    @Override
    public String eventType() {
        return ProductvariantDeleteEvent.EVENT_TYPE;
    }

    // Parse payload qua output port rồi giao nghiệp vụ dọn giỏ cho service.
    @Override
    public void handle(String event) {
        ProductvariantDeleteEvent payload = objectMapperPort.readValue(event, ProductvariantDeleteEvent.class);
        cartService.removeDeletedProductVariant(payload);
    }
}
