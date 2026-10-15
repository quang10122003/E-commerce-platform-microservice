package com.example.notification.adapter.in.kafka;

import com.example.notification.application.constant.Constant;
import com.example.notification.application.registry.AuthEventHandlerRegistry;
import com.example.notification.application.strategy.event.AuthEventHandler;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

// Consumer nhận event từ auth service và chuyển cho registry xử lý.
@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthConsumer {
    ObjectMapper objectMapper;
    AuthEventHandlerRegistry authEventHandlerRegistry;

    @KafkaListener(topics = Constant.AUTH_EVENTS_TOPIC)
    public void listen(String message) {
        try {
            var eventPayload = objectMapper.readTree(message);
            String eventType = eventPayload.path("eventType").asText();
            String eventId = eventPayload.path("eventId").asText();

            log.info("Nhận event auth: event_id={}, event_type={}", eventId, eventType);

            // Định tuyến event sang strategy phù hợp để không phải mở rộng consumer khi thêm event mới.
            AuthEventHandler handler = authEventHandlerRegistry.handle(eventType);
            if (handler == null) {
                log.warn("Bỏ qua event auth do không có handler: event_id={}, event_type={}",
                        eventId, eventType);
            } else {
                handler.handle(message);
                log.info("Xử lý event auth thành công: event_id={}, event_type={}", eventId, eventType);
            }
        } catch (Exception exception) {
            log.error("Xử lý event auth thất bại", exception);
            // Ném lỗi để Kafka có thể retry message thất bại.
            throw new IllegalStateException("Không thể xử lý event auth", exception);
        }
    }
}
