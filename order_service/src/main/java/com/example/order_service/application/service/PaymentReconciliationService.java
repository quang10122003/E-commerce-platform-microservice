package com.example.order_service.application.service;

import com.example.order_service.application.dto.response.VnpayQueryResult;
import com.example.order_service.application.port.in.ReconcileExpiredPaymentUseCase;
import com.example.order_service.application.port.out.client.VnpayQueryPort;
import com.example.order_service.application.port.out.repo.PaymentRepoPort;
import com.example.order_service.domain.model.OrderCancelledBy;
import com.example.order_service.domain.model.Payment;
import com.example.order_service.domain.model.PaymentStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class PaymentReconciliationService implements ReconcileExpiredPaymentUseCase {

    private static final int BATCH_SIZE = 50;

    private final PaymentRepoPort paymentRepoPort;
    private final VnpayQueryPort vnpayQueryPort;
    private final Clock clock;
    private final int graceMinutes;
    private final PaymentOutcomeService paymentOutcomeService;

    // Chờ thêm graceMinutes sau expires_at vì IPN có thể về trễ.
    @Override
    @Transactional(readOnly = true)
    public List<String> findExpiredPendingReferences() {
        LocalDateTime cutoff = LocalDateTime.now(clock).minusMinutes(graceMinutes);
        return paymentRepoPort.findPendingReferencesExpiredBefore(cutoff, BATCH_SIZE);
    }

    // Giữ khóa dòng trong lúc gọi QueryDR để IPN về cùng lúc phải chờ, không xác nhận hai lần.
    @Override
    @Transactional
    public void reconcile(String transactionReference) {
        Optional<Payment> found = paymentRepoPort.findByTransactionReferenceForUpdate(transactionReference);
        if (found.isEmpty() || found.get().getStatus() != PaymentStatus.PENDING) {
            log.info("Bỏ qua đối soát payment không còn PENDING: vnp_TxnRef={}", transactionReference);
            return;
        }
        Payment payment = found.get();
        LocalDateTime now = LocalDateTime.now(clock);
        if (payment.getRequestedAt() == null) {
            throw new IllegalStateException("Payment thiếu thời điểm tạo yêu cầu thanh toán để truy vấn VNPay");
        }
        VnpayQueryResult result = vnpayQueryPort.queryTransaction(transactionReference, payment.getRequestedAt(), now);

        if (result.isPaid()) {
            markPaid(payment, result, now);
        } else if (result.isUnpaid()) {
            cancel(payment, result, now);
        } else if (result.isProcessing()) {
            // Giữ PENDING để lần quét sau hỏi lại thay vì đoán kết quả.
            log.warn("Đối soát VNPay chưa có kết quả cuối: vnp_TxnRef={}, vnp_TransactionStatus={}",
                    transactionReference, result.transactionStatus());
        } else {
            throw new IllegalStateException("QueryDR VNPay trả mã lỗi " + result.responseCode());
        }
    }

    private void markPaid(Payment payment, VnpayQueryResult result, LocalDateTime now) {
        payment.markPaid(result.amount(), result.transactionNo(), null, result.bankCode(), null,
                result.responseCode(), result.transactionStatus(), result.payDate(), now);
        paymentOutcomeService.savePaidAndConfirmOrders(payment, now);
        log.info("Đối soát VNPay xác nhận đã thanh toán: vnp_TxnRef={}, payment_id={}",
                payment.getTransactionReference(), payment.getId());
    }

    private void cancel(Payment payment, VnpayQueryResult result, LocalDateTime now) {
        paymentOutcomeService.cancelPaymentAndOrders(payment, "VNPay không ghi nhận thanh toán thành công",
                OrderCancelledBy.SYSTEM, now);
        log.info("Đối soát VNPay hủy payment hết hạn: vnp_TxnRef={}, vnp_ResponseCode={}, vnp_TransactionStatus={}",
                payment.getTransactionReference(), result.responseCode(), result.transactionStatus());
    }
}
