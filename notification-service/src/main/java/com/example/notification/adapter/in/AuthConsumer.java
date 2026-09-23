package com.example.notification.adapter.in;

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

    @KafkaListener(topics = "${app.kafka.topic.auth-events}")
    public void listen(String message) {
        try {
            var eventPayload = objectMapper.readTree(message);
            String eventType = eventPayload.path("eventType").asText();

            log.info("Nhận event auth: event_type={}", eventType);

            // Định tuyến event sang strategy phù hợp để không phải mở rộng consumer khi thêm event mới.
            AuthEventHandler handler = authEventHandlerRegistry.handle(eventType);
            if (handler == null) {
                log.info("Bỏ qua event auth chưa được hỗ trợ: event_type={}", eventType);
            } else {
                handler.handle(message);
            }
        } catch (Exception exception) {
            log.error("Xử lý event auth thất bại", exception);
            // Ném lỗi để Kafka có thể retry message thất bại.
            throw new IllegalStateException("Không thể xử lý event auth", exception);
        }
    }
}
