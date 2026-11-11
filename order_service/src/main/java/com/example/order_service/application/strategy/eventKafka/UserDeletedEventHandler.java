package com.example.order_service.application.strategy.eventKafka;

import com.example.order_service.application.dto.event.UserDeletedEvent;
import com.example.order_service.application.port.out.serialization.ObjectMapperPort;
import com.example.order_service.application.service.UserAccountService;
import com.example.order_service.application.strategy.EventHandler;

public class UserDeletedEventHandler implements EventHandler<String> {
    private final ObjectMapperPort objectMapperPort;
    private final UserAccountService userAccountService;

    public UserDeletedEventHandler(ObjectMapperPort objectMapperPort, UserAccountService userAccountService) {
        this.objectMapperPort = objectMapperPort;
        this.userAccountService = userAccountService;
    }

    @Override
    public String eventType() {
        return UserDeletedEvent.EVENT_TYPE;
    }

    @Override
    public void handle(String message) {
        UserDeletedEvent event = objectMapperPort.readValue(message, UserDeletedEvent.class);
        if (event == null || event.userId() == null || event.userId() <= 0) {
            throw new IllegalArgumentException("Event xóa tài khoản thiếu userId hợp lệ");
        }
        userAccountService.removeDeletedUserData(event.userId());
    }
}
