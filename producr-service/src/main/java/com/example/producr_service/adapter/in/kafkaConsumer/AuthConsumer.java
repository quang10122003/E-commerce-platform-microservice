package com.example.producr_service.adapter.in.kafkaConsumer;

import com.example.common.untill.ValidationUtils;
import com.example.producr_service.application.constant.Constant;
import com.example.producr_service.application.port.in.ProcessAuthEventUseCase;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthConsumer {
    private final ObjectMapper objectMapper;
    private final ProcessAuthEventUseCase processAuthEventUseCase;

    @KafkaListener(topics = Constant.AUTH_EVENTS_TOPIC)
    public void listen(String message) {
        String eventId = null;
        String eventType = null;
        try {
            JsonNode payload = objectMapper.readTree(message);
            if (payload == null || !payload.isObject()) {
                throw new IllegalArgumentException("Event auth phải là JSON object");
            }
            JsonNode idNode = payload.path("eventId");
            JsonNode typeNode = payload.path("eventType");
            eventId = idNode.isTextual() ? idNode.textValue() : null;
            eventType = typeNode.isTextual() ? typeNode.textValue() : null;
            if (!ValidationUtils.hasText(eventType) || eventId == null
                    || !eventId.matches(Constant.BEHAVIOR_EVENT_ID_PATTERN)) {
                throw new IllegalArgumentException("Event auth thiếu eventId hoặc eventType hợp lệ");
            }
            log.info("Nhận event auth: topic={}, event_id={}, event_type={}",
                    Constant.AUTH_EVENTS_TOPIC, eventId, eventType);
            processAuthEventUseCase.process(eventId, eventType, message);
        } catch (Exception exception) {
            log.error("Xử lý event auth thất bại: topic={}, event_id={}, event_type={}, error_type={}",
                    Constant.AUTH_EVENTS_TOPIC, eventId, eventType, exception.getClass().getSimpleName());
            throw new IllegalStateException("Không thể xử lý event auth: event_id=%s, event_type=%s"
                    .formatted(eventId, eventType), exception);
        }
    }
}
