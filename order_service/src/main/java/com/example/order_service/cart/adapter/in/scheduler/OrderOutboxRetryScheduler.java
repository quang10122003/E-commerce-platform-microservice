package com.example.order_service.cart.adapter.in.scheduler;

import com.example.order_service.cart.application.dto.outbox.OutboxEventDto;
import com.example.order_service.cart.application.port.in.ProcessOrderOutboxUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderOutboxRetryScheduler {

    private final ProcessOrderOutboxUseCase processOrderOutboxUseCase;

    @Scheduled(fixedDelayString = "${app.outbox.fixed-delay-ms:5000}")
    public void processPendingEvents() {
        String previousRequestId = MDC.get("request_id");
        MDC.put("request_id", "scheduler-" + UUID.randomUUID());
        try {
            List<OutboxEventDto> events = processOrderOutboxUseCase.findPending();
            log.debug("Bắt đầu xử lý order outbox: số lượng={}", events.size());
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

    private void processEvent(OutboxEventDto event) {
        try {
            if (!processOrderOutboxUseCase.processEvent(event)) {
                return;
            }
            log.info(
                    "Xử lý order outbox thành công: event_type={}, aggregate_type={}, aggregate_id={}, event_id={}",
                    event.eventType(),
                    event.aggregateType(),
                    event.aggregateId(),
                    event.eventId()
            );
        } catch (Exception exception) {
            try {
                processOrderOutboxUseCase.markFailed(event.eventId(), exception.getClass().getSimpleName());
            } catch (Exception retryException) {
                log.error("Không thể ghi lịch retry: event_id={}, event_type={}, error_type={}",
                        event.eventId(), event.eventType(), retryException.getClass().getSimpleName());
            }
            log.error(
                    "Xử lý order outbox thất bại: event_type={}, aggregate_type={}, aggregate_id={}, event_id={}, retry={}, error_type={}",
                    event.eventType(),
                    event.aggregateType(),
                    event.aggregateId(),
                    event.eventId(),
                    event.retryCount() + 1,
                    exception.getClass().getSimpleName()
            );
        }
    }
}
