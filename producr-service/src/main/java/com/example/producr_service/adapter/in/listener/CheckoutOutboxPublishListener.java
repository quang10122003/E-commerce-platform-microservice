package com.example.producr_service.adapter.in.listener;

import com.example.producr_service.application.dto.outbox.OutboxEventDto;
import com.example.producr_service.application.port.in.PublishCheckoutOutboxUseCase;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class CheckoutOutboxPublishListener {

    private final PublishCheckoutOutboxUseCase publishCheckoutOutboxUseCase;
    // Pool riêng, không đăng ký bean Executor để không thay executor mặc định của Spring Boot.
    private final ExecutorService executor = new ThreadPoolExecutor(2, 8, 60, TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(1000), Thread.ofPlatform().name("checkout-outbox-", 0).factory());

    public CheckoutOutboxPublishListener(PublishCheckoutOutboxUseCase publishCheckoutOutboxUseCase) {
        this.publishCheckoutOutboxUseCase = publishCheckoutOutboxUseCase;
    }

    // Chạy ở thread khác vì trong pha AFTER_COMMIT transaction cũ vẫn gắn với thread, ghi DB tại đây sẽ không được commit.
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publishAfterCommit(OutboxEventDto event) {
        try {
            executor.execute(() -> publish(event));
        } catch (RejectedExecutionException exception) {
            log.warn("Hàng đợi gửi ngay outbox đã đầy, chờ bộ quét gửi: event_id={}, event_type={}",
                    event.eventId(), event.eventType());
        }
    }

    private void publish(OutboxEventDto event) {
        try {
            publishCheckoutOutboxUseCase.publishCheckoutEvent(event);
        } catch (RuntimeException exception) {
            // Event vẫn PENDING trong DB nên bộ quét định kỳ sẽ gửi lại; không đánh dấu lỗi để khỏi lùi lịch gửi.
            log.warn("Gửi ngay outbox thất bại, chờ bộ quét gửi lại: event_id={}, event_type={}, error={}",
                    event.eventId(), event.eventType(), exception.getMessage());
        }
    }

    @PreDestroy
    void shutdown() {
        executor.shutdown();
    }
}
