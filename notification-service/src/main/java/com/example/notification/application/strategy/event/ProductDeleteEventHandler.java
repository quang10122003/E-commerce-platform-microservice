package com.example.notification.application.strategy.event;

import com.example.notification.application.dto.event.ProductDeleteEvent;
import com.example.notification.application.service.ChatApplicationService;
import com.example.notification.application.port.out.objectMapper.ObjectMapperPort;

public class ProductDeleteEventHandler implements EventHandler {

    private final ChatApplicationService chatApplicationService;
    private final ObjectMapperPort objectMapperPort;

    public ProductDeleteEventHandler(
            ChatApplicationService chatApplicationService,
            ObjectMapperPort objectMapperPort
    ) {
        this.chatApplicationService = chatApplicationService;
        this.objectMapperPort = objectMapperPort;
    }

    @Override
    public String eventType() {
        return ProductDeleteEvent.EVENT_TYPE;
    }

    @Override
    public void handle(String message) {
        ProductDeleteEvent event = objectMapperPort.readValue(message, ProductDeleteEvent.class);
        chatApplicationService.deleteConversationsByProductId(event.productId());
    }
}
