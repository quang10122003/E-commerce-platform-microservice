package com.example.order_service.adapter.in.kafkaConsumer;

import com.example.common.untill.ValidationUtils;
import com.example.order_service.application.constant.Constant;
import com.example.order_service.application.port.in.ProcessCheckoutEventUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderConsumer {

    private final ObjectMapper objectMapper;
    private final ProcessCheckoutEventUseCase processCheckoutEventUseCase;

    @KafkaListener(topics = Constant.ORDER_EVENTS_TOPIC)
    public void listen(String event) {
        String eventId = null;
        String eventType = null;
        try {
            JsonNode payload = objectMapper.readTree(event);
            if (payload == null || !payload.isObject()) {
                throw new IllegalArgumentException("Event order phải là JSON object");
            }
            eventId = payload.path("eventId").stringValue();
            eventType = payload.path("eventType").stringValue();
            if (!ValidationUtils.hasText(eventId) || !ValidationUtils.hasText(eventType)) {
                throw new IllegalArgumentException("Event order thiếu eventId hoặc eventType hợp lệ");
            }
            UUID.fromString(eventId);
            log.info("Nhận event order: topic={}, event_id={}, event_type={}",
                    Constant.ORDER_EVENTS_TOPIC, eventId, eventType);
            processCheckoutEventUseCase.process(eventId, eventType, event);
        } catch (Exception exception) {
            log.error("Xử lý event order thất bại: topic={}, event_id={}, event_type={}",
                    Constant.ORDER_EVENTS_TOPIC, eventId, eventType, exception);
            throw new IllegalStateException(
                    "Không thể xử lý event order: event_id=%s, event_type=%s".formatted(eventId, eventType),
                    exception);
        }
    }
}
