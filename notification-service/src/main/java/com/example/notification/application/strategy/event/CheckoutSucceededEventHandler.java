package com.example.notification.application.strategy.event;

import com.example.notification.application.strategy.EventHandler;
import com.example.notification.application.dto.event.CheckoutSucceededNotification;
import com.example.notification.application.port.out.objectMapper.ObjectMapperPort;
import com.example.notification.application.service.CheckoutNotificationApplicationService;
import com.example.notification.application.service.NotificationApplicationService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CheckoutSucceededEventHandler implements EventHandler<String> {
    private final ObjectMapperPort objectMapperPort;
    private final CheckoutNotificationApplicationService notificationService;
    private final NotificationApplicationService emailService;

    @Override
    public String eventType() {
        return CheckoutSucceededNotification.EVENT_TYPE;
    }

    @Override
    public void handle(String message) {
        CheckoutSucceededNotification event = objectMapperPort.readValue(message, CheckoutSucceededNotification.class);
        notificationService.notifyCheckoutSucceeded(event);
        emailService.sendCheckoutSucceededEmail(event);
    }
}
