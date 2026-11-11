package com.example.notification.application.strategy.event;

import com.example.notification.application.dto.event.OrderCancelledByShopNotification;
import com.example.notification.application.port.out.objectMapper.ObjectMapperPort;
import com.example.notification.application.service.NotificationApplicationService;
import com.example.notification.application.strategy.EventHandler;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class OrderCancelledByShopEventHandler implements EventHandler<String> {
    private final ObjectMapperPort objectMapperPort;
    private final NotificationApplicationService emailService;

    @Override
    public String eventType() {
        return OrderCancelledByShopNotification.EVENT_TYPE;
    }

    @Override
    public void handle(String message) {
        emailService.sendOrderCancelledByShopEmail(objectMapperPort.readValue(message, OrderCancelledByShopNotification.class));
    }
}
