package com.example.order_service.order.adapter.in.kafkaConsumer;

import com.example.common.untill.ValidationUtils;
import com.example.order_service.cart.application.Constant.Constant;
import com.example.order_service.order.application.port.in.ProcessCheckoutEventUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class CheckoutConsumer {

    private final ObjectMapper objectMapper;
    private final ProcessCheckoutEventUseCase processCheckoutEventUseCase;

    @KafkaListener(id = "checkoutConsumer", topics = {Constant.ORDER_EVENTS_TOPIC, Constant.PRODUCT_EVENTS_TOPIC},
            groupId = "${app.kafka.checkout.group-id}",
            autoStartup = "${app.kafka.checkout.auto-startup:false}")
    public void listen(String event, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        String eventId = null;
        String eventType = null;
        try {
            JsonNode payload = objectMapper.readTree(event);
            if (payload == null || !payload.isObject()) {
                throw new IllegalArgumentException("Event checkout phải là JSON object");
            }
            eventId = payload.path("eventId").stringValue();
            eventType = payload.path("eventType").stringValue();
            if (!ValidationUtils.hasText(eventId) || !ValidationUtils.hasText(eventType)) {
                throw new IllegalArgumentException("Event checkout thiếu eventId hoặc eventType hợp lệ");
            }
            UUID.fromString(eventId);
            log.info("Nhận event checkout: topic={}, event_id={}, event_type={}",
                    topic, eventId, eventType);
            processCheckoutEventUseCase.process(eventId, eventType, event);
        } catch (Exception exception) {
            log.error("Xử lý event checkout thất bại: topic={}, event_id={}, event_type={}",
                    topic, eventId, eventType, exception);
            throw new IllegalStateException(
                    "Không thể xử lý event checkout: event_id=%s, event_type=%s".formatted(eventId, eventType),
                    exception);
        }
    }
}
