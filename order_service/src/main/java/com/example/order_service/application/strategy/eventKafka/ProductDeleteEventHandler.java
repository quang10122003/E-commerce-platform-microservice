package com.example.order_service.application.strategy.eventKafka;

import com.example.order_service.application.strategy.EventHandler;
import com.example.order_service.application.dto.event.ProductDeleteEvent;
import com.example.order_service.application.port.out.serialization.ObjectMapperPort;
import com.example.order_service.application.service.CartService;

public class ProductDeleteEventHandler implements EventHandler<String> {
    private final ObjectMapperPort objectMapperPort;
    private final CartService cartService;

    public ProductDeleteEventHandler(ObjectMapperPort objectMapperPort, CartService cartService) {
        this.objectMapperPort = objectMapperPort;
        this.cartService = cartService;
    }

    @Override
    public String eventType() {
        return ProductDeleteEvent.EVENT_TYPE;
    }

    @Override
    public void handle(String event) {
        ProductDeleteEvent productDeleteEvent = objectMapperPort.readValue(event, ProductDeleteEvent.class);
        cartService.removeDeletedProductVariants(productDeleteEvent);
    }
}
