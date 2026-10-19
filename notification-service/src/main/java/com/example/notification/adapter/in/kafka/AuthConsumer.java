package com.example.notification.adapter.in.kafka;

import com.example.notification.application.constant.Constant;
import com.example.common.untill.ValidationUtils;
import com.example.notification.application.port.in.ProcessAuthEventUseCase;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthConsumer {
    ObjectMapper objectMapper;
    ProcessAuthEventUseCase processAuthEventUseCase;

    @KafkaListener(topics = Constant.AUTH_EVENTS_TOPIC)
    public void listen(String message) {
        String eventId = null;
        String eventType = null;
        try {
            JsonNode eventPayload = objectMapper.readTree(message);
            if (eventPayload == null || !eventPayload.isObject()) {
                throw new IllegalArgumentException("Event auth phải là JSON object");
            }
            eventType = eventPayload.path("eventType").stringValue();
            eventId = eventPayload.path("eventId").stringValue();
            if (!ValidationUtils.hasText(eventType) || !ValidationUtils.hasText(eventId)) {
                throw new IllegalArgumentException("Event auth thiếu eventId hoặc eventType hợp lệ");
            }
            log.info("Nhận event auth: topic={}, event_id={}, event_type={}",
                    Constant.AUTH_EVENTS_TOPIC, eventId, eventType);
            processAuthEventUseCase.process(eventId, eventType, message);
        } catch (Exception exception) {
            log.error("Xử lý event auth thất bại: topic={}, event_id={}, event_type={}",
                    Constant.AUTH_EVENTS_TOPIC, eventId, eventType, exception);
            // Ném lỗi để Kafka có thể retry message thất bại.
            throw new IllegalStateException(
                    "Không thể xử lý event auth: event_id=%s, event_type=%s".formatted(eventId, eventType),
                    exception);
        }
    }
}
