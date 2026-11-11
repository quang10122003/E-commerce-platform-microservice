package com.example.order_service.application.strategy.eventKafka;

import com.example.order_service.application.strategy.EventHandler;
import com.example.order_service.application.dto.event.ProductDeleteEvent;
import com.example.order_service.application.port.out.serialization.ObjectMapperPort;
import com.example.order_service.application.service.CartService;
import com.example.order_service.application.service.OrderItemReferenceService;

public class ProductDeleteEventHandler implements EventHandler<String> {
    private final ObjectMapperPort objectMapperPort;
    private final CartService cartService;
    private final OrderItemReferenceService orderItemReferenceService;

    public ProductDeleteEventHandler(ObjectMapperPort objectMapperPort, CartService cartService,
                                     OrderItemReferenceService orderItemReferenceService) {
        this.objectMapperPort = objectMapperPort;
        this.cartService = cartService;
        this.orderItemReferenceService = orderItemReferenceService;
    }

    @Override
    public String eventType() {
        return ProductDeleteEvent.EVENT_TYPE;
    }

    @Override
    public void handle(String event) {
        ProductDeleteEvent productDeleteEvent = objectMapperPort.readValue(event, ProductDeleteEvent.class);
        cartService.removeDeletedProductVariants(productDeleteEvent);
        // product_variant_id do event xóa từng variant xử lý, ở đây chỉ ngắt product_id.
        orderItemReferenceService.clearDeletedProduct(productDeleteEvent.productId());
    }
}
