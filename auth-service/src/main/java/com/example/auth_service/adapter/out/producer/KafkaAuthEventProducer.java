package com.example.auth_service.adapter.out.producer;

import com.example.auth_service.application.DTO.OutboxEventDto;
import com.example.auth_service.application.constant.Constant;
import com.example.auth_service.application.port.out.AuthEventPublisherPort;
import com.example.common.untill.JsonUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

// Adapter phát hành event auth bằng Kafka.
@Component
@RequiredArgsConstructor
public class KafkaAuthEventProducer implements AuthEventPublisherPort {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final JsonUtils jsonUtils;

    // Chờ Kafka xác nhận gửi và truyền lỗi về flow xử lý outbox.
    @Override
    public void publish(OutboxEventDto event) {
        try {
            kafkaTemplate.send(
                            Constant.AUTH_EVENTS_TOPIC,
                            event.eventId().toString(),
                            jsonUtils.toJson(event.payload()))
                    .get(20, TimeUnit.SECONDS);
        } catch (InterruptedException exception) {
            // Giữ tín hiệu ngắt khi chuyển lỗi sang RuntimeException.
            Thread.currentThread().interrupt();
            throw new IllegalStateException(
                    "Bị gián đoạn khi phát hành event auth: event_type=%s, event_id=%s"
                            .formatted(event.eventType(), event.eventId()),
                    exception
            );
        } catch (Exception exception) {
            throw new IllegalStateException(
                    "Không thể phát hành event auth: event_type=%s, event_id=%s"
                            .formatted(event.eventType(), event.eventId()),
                    exception
            );
        }
    }
}
