package com.example.producr_service.adapter.in.scheduler;

import com.example.producr_service.application.dto.outbox.OutboxEventDto;
import com.example.producr_service.application.port.out.outbox.OutboxPort;
import com.example.producr_service.application.registry.OutboxEventHandlerRegistry;
import com.example.producr_service.application.strategy.outbox.OutboxEventHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

// Scheduler nhận event đến hạn và đưa vào application xử lý retry.
@Component
@RequiredArgsConstructor
@Slf4j
public class ProductOutboxRetryScheduler {

    private final OutboxPort outboxPort;
    private final OutboxEventHandlerRegistry outboxEventHandlerRegistry;

    @Scheduled(fixedDelayString = "${app.outbox.fixed-delay-ms:5000}")
    public void processPendingEvents() {
        String previousRequestId = MDC.get("request_id");
        MDC.put("request_id", "scheduler-" + UUID.randomUUID());
        try {
            List<OutboxEventDto> events = outboxPort.findPending();
            log.debug("Bắt đầu xử lý product outbox: số lượng={}", events.size());
            events.forEach(this::processEvent);
        } finally {
            // Khôi phục MDC để không làm nhiễm request tiếp theo trên cùng thread.
            if (previousRequestId == null) {
                MDC.remove("request_id");
            } else {
                MDC.put("request_id", previousRequestId);
            }
        }
    }

    // Chạy strategy tương ứng và cập nhật trạng thái retry của event.
    private void processEvent(OutboxEventDto event) {
        try {
            OutboxEventHandler handler = outboxEventHandlerRegistry.handle(event.eventType());
            handler.handle(event);
            outboxPort.markPublished(event.eventId());
            log.info(
                    "Xử lý product outbox thành công: event_type={}, aggregate_type={}, aggregate_id={}, event_id={}",
                    event.eventType(),
                    event.aggregateType(),
                    event.aggregateId(),
                    event.eventId()
            );
        } catch (Exception exception) {
            outboxPort.markFailed(event.eventId(), exception.getMessage());
            log.error(
                    "Xử lý product outbox thất bại: event_type={}, aggregate_type={}, aggregate_id={}, event_id={}, retry={}",
                    event.eventType(),
                    event.aggregateType(),
                    event.aggregateId(),
                    event.eventId(),
                    event.retryCount() + 1,
                    exception
            );
        }
    }
}
