package com.example.auth_service.adapter.out;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.auth_service.application.DTO.OutboxEventDto;
import com.example.auth_service.application.port.out.OutboxEventPort;
import com.example.common.untill.JsonUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaOutboxPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final OutboxEventPort outboxEventPort;
    private final JsonUtils jsonUtils;

    @Value("${app.kafka.topic.auth-events}")
    private String authEventsTopic;


    // Poll lấy event đang Pending theo định kì push kafka 
    @Scheduled(fixedDelayString = "${app.kafka.publisher.fixed-delay-ms:5000}")
    public void publishPendingEvents() {
        List<OutboxEventDto> events = outboxEventPort.findPending();
        System.out.println(" lấy đc"+events.size());
        events.forEach(this::publish);
    }

    // Chỉ đánh dấu PUBLISHED sau khi Kafka xác nhận gửi thành công.
    private void publish(OutboxEventDto event) {
        try {
            kafkaTemplate.send(authEventsTopic, event.eventId().toString(), jsonUtils.toJson(event
                    .payload()))
                    .get(10, TimeUnit.SECONDS);
            outboxEventPort.markPublished(event.eventId());
        } catch (Exception exception) {
            log.error("Publish outbox thất bại, eventId={}", event.eventId(), exception);
            outboxEventPort.markFailed(event.eventId(), exception.getMessage());
        }
    }

   
}
