package com.example.order_service.adapter.in.scheduler;

import com.example.order_service.application.port.in.AutoCompleteOrderUseCase;
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
// Quét đơn đang giao đủ thời gian để chuyển sang hoàn thành.
public class AutoCompleteOrderScheduler {

    private final AutoCompleteOrderUseCase autoCompleteOrderUseCase;

    @Scheduled(fixedDelayString = "${app.order.auto-complete-fixed-delay-ms:300000}")
    public void completeShippedOrders() {
        String previousRequestId = MDC.get("request_id");
        MDC.put("request_id", "scheduler-" + UUID.randomUUID());
        try {
            List<Long> orderIds = autoCompleteOrderUseCase.findDueOrderIds();
            log.debug("Bắt đầu tự hoàn thành đơn đang giao: số lượng={}", orderIds.size());
            orderIds.forEach(this::complete);
        } finally {
            // Khôi phục MDC để không làm nhiễm request tiếp theo trên cùng thread.
            if (previousRequestId == null) {
                MDC.remove("request_id");
            } else {
                MDC.put("request_id", previousRequestId);
            }
        }
    }

    // Lỗi của một đơn không chặn các đơn còn lại; đơn vẫn SHIPPING nên lần quét sau thử lại.
    private void complete(Long orderId) {
        try {
            autoCompleteOrderUseCase.complete(orderId);
        } catch (Exception exception) {
            log.error("Tự hoàn thành đơn thất bại: order_id={}, error_type={}, error={}",
                    orderId, exception.getClass().getSimpleName(), exception.getMessage());
        }
    }
}
