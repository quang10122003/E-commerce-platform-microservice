package com.example.auth_service.adapter.out.messaging.producer;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.slf4j.MDC;
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
        String previousRequestId = MDC.get("request_id");
        MDC.put("request_id", "scheduler-" + UUID.randomUUID());
        try {
            List<OutboxEventDto> events = outboxEventPort.findPending();
            log.debug("Bắt đầu phát hành outbox event: số lượng={}", events.size());
            events.forEach(this::publish);
        } finally {
            // Khôi phục MDC để không làm nhiễm request tiếp theo trên cùng thread.
            if (previousRequestId == null) {
                MDC.remove("request_id");
            } else {
                MDC.put("request_id", previousRequestId);
            }
        }
    }

    // Chỉ đánh dấu PUBLISHED sau khi Kafka xác nhận gửi thành công.
    private void publish(OutboxEventDto event) {
        try {
            kafkaTemplate.send(authEventsTopic, event.eventId().toString(), jsonUtils.toJson(event
                    .payload()))
                    .get(10, TimeUnit.SECONDS);
            outboxEventPort.markPublished(event.eventId());
        } catch (Exception exception) {
            log.error("Phát hành outbox event thất bại: event_id={}, topic={}",
                    event.eventId(), authEventsTopic, exception);
            outboxEventPort.markFailed(event.eventId(), exception.getMessage());
        }
    }

   
}
