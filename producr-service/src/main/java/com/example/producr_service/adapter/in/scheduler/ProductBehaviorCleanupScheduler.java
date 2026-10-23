package com.example.producr_service.adapter.in.scheduler;

import com.example.producr_service.application.port.in.CleanupExpiredProductBehaviorsUseCase;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
// jod chạy mỗi ngày scan xóa các hanh vi cũ của người dùng quá 30 ngày
public class ProductBehaviorCleanupScheduler {
    private final CleanupExpiredProductBehaviorsUseCase cleanupExpiredProductBehaviorsUseCase;

    @Scheduled(cron = "${app.recommendation.behavior-cleanup.cron}", zone = "#{@clock.zone.id}")
    public void cleanupExpiredBehaviors() {
        String previousRequestId = MDC.get("request_id");
        MDC.put("request_id", "scheduler-" + UUID.randomUUID());
        try {
            log.debug("Bắt đầu dọn hành vi sản phẩm hết hạn");
            cleanupExpiredProductBehaviorsUseCase.cleanupExpiredBehaviors();
        } catch (RuntimeException exception) {
            log.error("Dọn hành vi sản phẩm hết hạn thất bại", exception);
            throw exception;
        } finally {
            // Khôi phục MDC để không làm nhiễm request tiếp theo trên cùng thread.
            if (previousRequestId == null) {
                MDC.remove("request_id");
            } else {
                MDC.put("request_id", previousRequestId);
            }
        }
    }
}
