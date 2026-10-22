package com.example.notification.application.strategy.event;

import com.example.notification.application.dto.event.UserRegisteredEvent;
import com.example.notification.application.service.NotificationApplicationService;
import com.example.notification.application.port.out.objectMapper.ObjectMapperPort;

public class UserRegisteredEventHandler implements EventHandler {

    private final NotificationApplicationService notificationApplicationService;
    private final ObjectMapperPort objectMapperPort;

    public UserRegisteredEventHandler(
            NotificationApplicationService notificationApplicationService,
            ObjectMapperPort objectMapperPort
    ) {
        this.notificationApplicationService = notificationApplicationService;
        this.objectMapperPort = objectMapperPort;
    }

    @Override
    public String eventType() {
        return UserRegisteredEvent.EVENT_TYPE;
    }

    @Override
    public void handle(String message) {
        UserRegisteredEvent event = objectMapperPort.readValue(message, UserRegisteredEvent.class);
        notificationApplicationService.sendEmailUserRegisteredUse(event);
    }
}
