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
import lombok.extern.slf4j.Slf4j;

@Slf4j
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

            log.info(
                    "Nhận event đăng ký người dùng: event_id={}, event_type={}",
                    event.eventId(),
                    event.eventType()
            );

            if (EventType.USER_REGISTERED.getValue().equals(event.eventType())) {
                emailUserRegisteredUseCase.sendEmailUserRegisteredUse(event);
                log.info(
                        "Xử lý event đăng ký người dùng thành công: event_id={}",
                        event.eventId()
                );
            } else {
                log.warn(
                        "Bỏ qua event không được hỗ trợ: event_id={}, event_type={}",
                        event.eventId(),
                        event.eventType()
                );
            }
        } catch (Exception exception) {
            log.error("Xử lý event đăng ký người dùng thất bại", exception);
            // Ném lỗi để Kafka có thể retry message thất bại.
            throw new IllegalStateException(
                    "Không thể xử lý UserRegisteredEvent",
                    exception);
        }
    }
}
