package com.example.order_service.cart.application.strategy.eventKafka;

import com.example.order_service.cart.application.dto.event.ProductDeleteEvent;
import com.example.order_service.cart.application.port.out.objettMapper.ObjectMapperPort;
import com.example.order_service.cart.application.service.CartService;

public class ProductDeleteEventHandler implements EventHandler {
    private final ObjectMapperPort objectMapperPort;
    private final CartService cartService;

    // Handler nội bộ application gọi service nghiệp vụ, không vòng lại input port.
    public ProductDeleteEventHandler(ObjectMapperPort objectMapperPort, CartService cartService) {
        this.objectMapperPort = objectMapperPort;
        this.cartService = cartService;
    }

    // Khai báo loại event do strategy phụ trách.
    @Override
    public String eventType() {
        return ProductDeleteEvent.EVENT_TYPE;
    }

    // Parse payload qua output port rồi giao nghiệp vụ dọn giỏ cho service.
    @Override
    public void handle(String event) {
        ProductDeleteEvent productDeleteEvent = objectMapperPort.readValue(event, ProductDeleteEvent.class);
        cartService.removeDeletedProductVariants(productDeleteEvent);
    }
}
