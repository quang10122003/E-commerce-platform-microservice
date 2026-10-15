package com.example.order_service.cart.adapter.in.kafkaConsumer;

import com.example.order_service.cart.application.Constant.Constant;
import com.example.common.untill.ValidationUtils;
import com.example.order_service.cart.application.port.in.ProcessProductEventUseCase;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Component
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Slf4j
public class ProductConsumer {

    ObjectMapper objectMapper;
    ProcessProductEventUseCase processProductEventUseCase;

    // Đọc metadata tại adapter và gọi application duy nhất qua input port.
    @KafkaListener(topics = Constant.PRODUCT_EVENTS_TOPIC)
    public void listen(String event) {
        String eventId = null;
        String eventType = null;
        try {
            JsonNode payload = objectMapper.readTree(event);
            if (payload == null || !payload.isObject()) {
                throw new IllegalArgumentException("Event product phải là JSON object");
            }

            eventType = payload.path("eventType").stringValue();
            eventId = payload.path("eventId").stringValue();

            if (!ValidationUtils.hasText(eventType) || !ValidationUtils.hasText(eventId)) {
                throw new IllegalArgumentException("Event product thiếu eventId hoặc eventType hợp lệ");
            }

            log.info("Nhận event product: topic={}, event_id={}, event_type={}",
                    Constant.PRODUCT_EVENTS_TOPIC, eventId, eventType);

            processProductEventUseCase.process(eventId, eventType, event);
        } catch (Exception exception) {
            log.error("Xử lý event product thất bại: topic={}, event_id={}, event_type={}",
                    Constant.PRODUCT_EVENTS_TOPIC, eventId, eventType, exception);
            // Ném lỗi để Kafka có thể retry message thất bại.
            throw new IllegalStateException(
                    "Không thể xử lý event product: event_id=%s, event_type=%s".formatted(eventId, eventType),
                    exception);
        }
    }
}
