package com.example.auth_service.adapter.in.scheduler;

import com.example.auth_service.application.DTO.OutboxEventDto;
import com.example.auth_service.application.port.in.ProcessAuthOutboxUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

// Scheduler lấy event pending và đưa vào application qua registry xử lý.
@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxPublisher {

    private final ProcessAuthOutboxUseCase processAuthOutboxUseCase;

    @Scheduled(fixedDelayString = "${app.kafka.publisher.fixed-delay-ms:5000}")
    public void publishPendingEvents() {
        String previousRequestId = MDC.get("request_id");
        MDC.put("request_id", "scheduler-" + UUID.randomUUID());
        try {
            List<OutboxEventDto> events = processAuthOutboxUseCase.findPending();
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

    // Gọi strategy tương ứng và chỉ đánh dấu thành công sau khi phát hành hoàn tất.
    private void publish(OutboxEventDto event) {
        try {
            processAuthOutboxUseCase.processEvent(event);
        } catch (Exception exception) {
            log.error(
                    "Phát hành outbox event thất bại: event_id={}, event_type={}",
                    event.eventId(),
                    event.eventType(),
                    exception
            );
            processAuthOutboxUseCase.markFailed(event.eventId(), exception.getMessage());
        }
    }
}
