package com.example.notification.application.strategy.event;

import com.example.notification.application.dto.event.OrderStatusChangedNotification;
import com.example.notification.application.port.out.objectMapper.ObjectMapperPort;
import com.example.notification.application.service.NotificationApplicationService;
import com.example.notification.application.strategy.EventHandler;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class OrderStatusChangedEventHandler implements EventHandler<String> {
    private final ObjectMapperPort objectMapperPort;
    private final NotificationApplicationService emailService;

    @Override
    public String eventType() {
        return OrderStatusChangedNotification.EVENT_TYPE;
    }

    @Override
    public void handle(String message) {
        emailService.sendOrderStatusChangedEmail(objectMapperPort.readValue(message, OrderStatusChangedNotification.class));
    }
}
