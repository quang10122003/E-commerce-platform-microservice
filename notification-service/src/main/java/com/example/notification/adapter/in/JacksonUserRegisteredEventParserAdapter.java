package com.example.notification.adapter.in;

import com.example.notification.application.dto.event.UserRegisteredEvent;
import com.example.notification.application.port.out.UserRegisteredEventParserPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

// Adapter dùng Jackson để chuyển message broker thành event application.
@Component
@RequiredArgsConstructor
public class JacksonUserRegisteredEventParserAdapter implements UserRegisteredEventParserPort {

    private final ObjectMapper objectMapper;

    @Override
    public UserRegisteredEvent parse(String message) {
        try {
            return objectMapper.readValue(message, UserRegisteredEvent.class);
        } catch (Exception exception) {
            throw new IllegalStateException(
                    "Không thể parse UserRegisteredEvent",
                    exception
            );
        }
    }
}
