package com.example.notification.application.strategy.event;

import com.example.notification.application.strategy.EventHandler;
import com.example.notification.application.dto.event.CheckoutFailedNotification;
import com.example.notification.application.port.out.objectMapper.ObjectMapperPort;
import com.example.notification.application.service.CheckoutNotificationApplicationService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CheckoutFailedEventHandler implements EventHandler<String> {
    private final ObjectMapperPort objectMapperPort;
    private final CheckoutNotificationApplicationService notificationService;

    @Override
    public String eventType() {
        return CheckoutFailedNotification.EVENT_TYPE;
    }

    @Override
    public void handle(String message) {
        notificationService.notifyCheckoutFailed(
                objectMapperPort.readValue(message, CheckoutFailedNotification.class));
    }
}
