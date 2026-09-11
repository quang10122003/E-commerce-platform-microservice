package com.example.notification.adapter.in;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import tools.jackson.databind.ObjectMapper;
import com.example.notification.application.dto.event.EventType;
import com.example.notification.application.dto.event.UserRegisteredEvent;
import com.example.notification.application.port.in.SendEmailUserRegisteredUseCase;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Component 
@RequiredArgsConstructor 
@FieldDefaults (level = AccessLevel.PRIVATE,makeFinal = true)
public class AuthConsumer {
    SendEmailUserRegisteredUseCase emailUserRegisteredUseCase;
    ObjectMapper objectMapper;

    @KafkaListener(
            topics = "${app.kafka.topic.auth-events}")
    public void listen(String message) {
        try {
            UserRegisteredEvent event = objectMapper.readValue(
                    message,
                    UserRegisteredEvent.class);

            if (EventType.USER_REGISTERED.getValue().equals(event.eventType())) {
                emailUserRegisteredUseCase.sendEmailUserRegisteredUse(event);
            }
        } catch (Exception exception) {
            // Ném lỗi để Kafka có thể retry message thất bại.
            throw new IllegalStateException(
                    "Không thể xử lý UserRegisteredEvent",
                    exception);
        }
    }
}
