package com.example.notification.application.strategy.event;

import com.example.notification.application.dto.event.UserRegisteredEvent;
import com.example.notification.application.port.in.SendEmailUserRegisteredUseCase;
import com.example.notification.application.port.out.objectMapper.UserRegisteredEventParserPort;

// Xử lý event đăng ký người dùng và gửi email chào mừng.
public class UserRegisteredEventHandler implements AuthEventHandler {

    private final SendEmailUserRegisteredUseCase emailUserRegisteredUseCase;
    private final UserRegisteredEventParserPort eventParserPort;

    public UserRegisteredEventHandler(
            SendEmailUserRegisteredUseCase emailUserRegisteredUseCase,
            UserRegisteredEventParserPort eventParserPort
    ) {
        this.emailUserRegisteredUseCase = emailUserRegisteredUseCase;
        this.eventParserPort = eventParserPort;
    }

    @Override
    public String eventType() {
        return UserRegisteredEvent.EVENT_TYPE;
    }

    @Override
    public void handle(String message) {
        UserRegisteredEvent event = eventParserPort.parse(message);
        emailUserRegisteredUseCase.sendEmailUserRegisteredUse(event);
    }
}
