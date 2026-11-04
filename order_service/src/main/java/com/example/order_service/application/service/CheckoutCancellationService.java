package com.example.order_service.application.service;

import com.example.order_service.application.port.out.repo.CheckoutRepoPort;
import com.example.order_service.application.dto.event.CheckoutFailedPayload;
import com.example.order_service.application.port.out.repo.PaymentRepoPort;
import com.example.order_service.domain.model.Checkout;
import com.example.order_service.domain.model.CheckoutStatus;
import com.example.order_service.domain.model.Payment;
import com.example.order_service.domain.model.PaymentStatus;
import com.example.order_service.application.port.out.outbox.OutboxPort;
import com.example.order_service.application.dto.outbox.OutboxEventDto;
import com.example.order_service.application.constant.Constant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Slf4j
public class CheckoutCancellationService {

    private final CheckoutRepoPort checkoutRepoPort;
    private final PaymentRepoPort paymentRepoPort;
    private final Clock clock;
    private final OutboxPort outbox;

    @Transactional
    public void cancelInvalidRequest(Long checkoutId, String reason) {
        cancelCheckout(checkoutId, Constant.CHECKOUT_INVALID_REQUEST, reason);
    }

    // hàm đánh dấu cancel check out và payement nếu có kèm gửi event CheckoutFailed về notification
    @Transactional
    public void cancelCheckout(Long checkoutId, String reasonCode, String reason) {
        Optional<Checkout> existing = checkoutRepoPort.findByIdForUpdate(checkoutId);
        if (existing.isEmpty()) {
            log.warn("Bỏ qua hủy checkout không tồn tại: checkout_id={}", checkoutId);
            return;
        }
        Checkout checkout = existing.get();
        if (checkout.getStatus() != CheckoutStatus.PROCESSING) {
            log.info("Bỏ qua hủy checkout đã kết thúc: checkout_id={}, status={}", checkoutId, checkout.getStatus());
            return;
        }
        Optional<Payment> payment = paymentRepoPort.findByCheckoutIdForUpdate(checkoutId);
        // Giao dịch đã bắt đầu cần đối soát với provider, không hủy chỉ vì nhận event sai dữ liệu.
        if (payment.isPresent() && payment.get().getStatus() != PaymentStatus.UNPAID
                && payment.get().getStatus() != PaymentStatus.CANCEL) {
            log.warn("Không tự hủy checkout có giao dịch đã bắt đầu: checkout_id={}, payment_status={}",
                    checkoutId, payment.get().getStatus());
            return;
        }
        LocalDateTime now = LocalDateTime.now(clock);
        reason = reason == null || reason.isBlank() ? "Không thể tiếp tục checkout" : reason;
        checkout.cancel(reason, now);
        checkoutRepoPort.save(checkout);
        if (payment.isPresent() && payment.get().getStatus() == PaymentStatus.UNPAID) {
            payment.get().cancel(reason, now);
            paymentRepoPort.save(payment.get());
        }
        UUID eventId = UUID.randomUUID();
        CheckoutFailedPayload payload = new CheckoutFailedPayload(eventId, CheckoutFailedPayload.EVENT_TYPE,
                checkoutId, checkout.getStatus(), reasonCode, reason,
                now.atZone(clock.getZone()).toOffsetDateTime());
        outbox.save(new OutboxEventDto(eventId, Constant.CHECKOUT, checkoutId.toString(),
                payload.eventType(), payload, now));
        log.info("Đã ghi hủy checkout do event không hợp lệ: checkout_id={}", checkoutId);
    }
}
