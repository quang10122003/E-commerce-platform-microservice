package com.example.order_service.application.strategy.eventKafka;

import com.example.order_service.application.strategy.EventHandler;
import com.example.order_service.application.dto.event.ProductvariantDeleteEvent;
import com.example.order_service.application.port.out.serialization.ObjectMapperPort;
import com.example.order_service.application.service.CartService;
import com.example.order_service.application.service.OrderItemReferenceService;

public class ProductvariantDeleteEventHandler implements EventHandler<String> {
    private final ObjectMapperPort objectMapperPort;
    private final CartService cartService;
    private final OrderItemReferenceService orderItemReferenceService;

    public ProductvariantDeleteEventHandler(ObjectMapperPort objectMapperPort, CartService cartService,
                                            OrderItemReferenceService orderItemReferenceService) {
        this.objectMapperPort = objectMapperPort;
        this.cartService = cartService;
        this.orderItemReferenceService = orderItemReferenceService;
    }

    @Override
    public String eventType() {
        return ProductvariantDeleteEvent.EVENT_TYPE;
    }

    @Override
    public void handle(String event) {
        ProductvariantDeleteEvent payload = objectMapperPort.readValue(event, ProductvariantDeleteEvent.class);
        cartService.removeDeletedProductVariant(payload);
        orderItemReferenceService.clearDeletedVariant(payload.variantId());
    }
}
