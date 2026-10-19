package com.example.notification.application.port.out.objectMapper;

import com.example.notification.application.dto.event.UserRegisteredEvent;

public interface UserRegisteredEventParserPort {

    UserRegisteredEvent parse(String message);
}
