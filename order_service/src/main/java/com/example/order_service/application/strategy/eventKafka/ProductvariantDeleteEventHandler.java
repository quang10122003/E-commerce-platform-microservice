package com.example.order_service.application.strategy.eventKafka;

import com.example.order_service.application.strategy.EventHandler;
import com.example.order_service.application.dto.event.ProductvariantDeleteEvent;
import com.example.order_service.application.port.out.serialization.ObjectMapperPort;
import com.example.order_service.application.service.CartService;

public class ProductvariantDeleteEventHandler implements EventHandler<String> {
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
