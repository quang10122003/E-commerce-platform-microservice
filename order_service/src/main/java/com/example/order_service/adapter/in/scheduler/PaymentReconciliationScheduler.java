package com.example.order_service.adapter.in.scheduler;

import com.example.order_service.application.port.in.ReconcileExpiredPaymentUseCase;
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
// đối soát giao dịch
public class PaymentReconciliationScheduler {

    private final ReconcileExpiredPaymentUseCase reconcileExpiredPaymentUseCase;

    @Scheduled(fixedDelayString = "${app.vnpay.reconcile-fixed-delay-ms:60000}")
    public void reconcileExpiredPayments() {
        String previousRequestId = MDC.get("request_id");
        MDC.put("request_id", "scheduler-" + UUID.randomUUID());
        try {
            List<String> references = reconcileExpiredPaymentUseCase.findExpiredPendingReferences();
            log.debug("Bắt đầu đối soát payment hết hạn: số lượng={}", references.size());
            references.forEach(this::reconcile);
        } finally {
            // Khôi phục MDC để không làm nhiễm request tiếp theo trên cùng thread.
            if (previousRequestId == null) {
                MDC.remove("request_id");
            } else {
                MDC.put("request_id", previousRequestId);
            }
        }
    }

    // Lỗi của một payment không được chặn các payment còn lại; payment vẫn PENDING nên lần quét sau thử lại.
    private void reconcile(String transactionReference) {
        try {
            reconcileExpiredPaymentUseCase.reconcile(transactionReference);
        } catch (Exception exception) {
            log.error("Đối soát payment thất bại: vnp_TxnRef={}, error_type={}, error={}",
                    transactionReference, exception.getClass().getSimpleName(), exception.getMessage());
        }
    }
}
