package com.example.notification.application.strategy.event;

import com.example.notification.application.dto.event.UserRegisteredEvent;
import com.example.notification.application.service.NotificationApplicationService;
import com.example.notification.application.port.out.objectMapper.UserRegisteredEventParserPort;

public class UserRegisteredEventHandler implements EventHandler {

    private final NotificationApplicationService notificationApplicationService;
    private final UserRegisteredEventParserPort eventParserPort;

    public UserRegisteredEventHandler(
            NotificationApplicationService notificationApplicationService,
            UserRegisteredEventParserPort eventParserPort
    ) {
        this.notificationApplicationService = notificationApplicationService;
        this.eventParserPort = eventParserPort;
    }

    @Override
    public String eventType() {
        return UserRegisteredEvent.EVENT_TYPE;
    }

    @Override
    public void handle(String message) {
        UserRegisteredEvent event = eventParserPort.parse(message);
        notificationApplicationService.sendEmailUserRegisteredUse(event);
    }
}
