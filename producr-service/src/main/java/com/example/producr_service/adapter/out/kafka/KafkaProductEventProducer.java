package com.example.producr_service.adapter.out.kafka;

import com.example.common.untill.JsonUtils;
import com.example.producr_service.application.constant.Constant;
import com.example.producr_service.application.dto.outbox.OutboxEventDto;
import com.example.producr_service.application.port.out.kafka.ProductEventPublisherPort;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class KafkaProductEventProducer implements ProductEventPublisherPort {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final JsonUtils jsonUtils;
    @Override
    public void publish(OutboxEventDto event) {
        try {
            kafkaTemplate.send(Constant.PRODUCT_EVENTS_TOPIC,event.eventId().toString(),jsonUtils.toJson(event.payload())).get(20, TimeUnit.SECONDS);
        }catch (InterruptedException exception) {
            // Giữ tín hiệu ngắt khi chuyển lỗi sang RuntimeException.
            Thread.currentThread().interrupt();
            throw new IllegalStateException(
                    "Bị gián đoạn khi phát hành event product: event_type=%s, event_id=%s"
                            .formatted(event.eventType(), event.eventId()),
                    exception
            );
        } catch (Exception exception) {
            throw new IllegalStateException(
                    "Không thể phát hành event product: event_type=%s, event_id=%s"
                            .formatted(event.eventType(), event.eventId()),
                    exception
            );
        }

    }
}
