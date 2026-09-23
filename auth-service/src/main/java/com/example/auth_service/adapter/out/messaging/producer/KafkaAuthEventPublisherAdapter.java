package com.example.auth_service.adapter.out.messaging.producer;

import com.example.auth_service.application.DTO.OutboxEventDto;
import com.example.auth_service.application.port.out.AuthEventPublisherPort;
import com.example.common.untill.JsonUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

// Adapter phát hành event auth bằng Kafka.
@Component
@RequiredArgsConstructor
public class KafkaAuthEventPublisherAdapter implements AuthEventPublisherPort {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final JsonUtils jsonUtils;

    @Value("${app.kafka.topic.auth-events}")
    private String authEventsTopic;

    @Override
    public void publish(OutboxEventDto event) {
        try {
            kafkaTemplate.send(
                            authEventsTopic,
                            event.eventId().toString(),
                            jsonUtils.toJson(event.payload()))
                    .get(10, TimeUnit.SECONDS);
        } catch (Exception exception) {
            throw new IllegalStateException(
                    "Không thể phát hành event auth: " + event.eventType(),
                    exception
            );
        }
    }
}
