package com.example.order_service.cart.application.strategy.eventKafka;

import com.example.order_service.cart.application.dto.event.ProductvariantDeleteEvent;
import com.example.order_service.cart.application.port.out.objettMapper.ObjectMapperPort;
import com.example.order_service.cart.application.service.CartService;

public class ProductvariantDeleteEventHandler implements EventHandler {
    private final ObjectMapperPort objectMapperPort;
    private final CartService cartService;

    public ProductvariantDeleteEventHandler(ObjectMapperPort objectMapperPort, CartService cartService) {
        this.objectMapperPort = objectMapperPort;
        this.cartService = cartService;
    }

    @Override
    public String eventType() {
        return ProductvariantDeleteEvent.EVENT_TYPE;
    }

    @Override
    public void handle(String event) {
        ProductvariantDeleteEvent payload = objectMapperPort.readValue(event, ProductvariantDeleteEvent.class);
        cartService.removeDeletedProductVariant(payload);
    }
}
