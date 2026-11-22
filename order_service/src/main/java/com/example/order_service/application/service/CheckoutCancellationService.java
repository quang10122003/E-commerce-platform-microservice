package com.example.order_service.application.service;

import com.example.order_service.application.port.out.repo.CheckoutRepoPort;
import com.example.order_service.application.dto.event.CheckoutFailedPayload;
import com.example.order_service.application.port.out.repo.PaymentRepoPort;
import com.example.order_service.domain.model.Checkout;
import com.example.order_service.domain.model.CheckoutStatus;
import com.example.order_service.domain.model.Payment;
import com.example.order_service.domain.model.PaymentStatus;
import com.example.order_service.application.port.out.outbox.OutboxPort;
import com.example.order_service.application.port.out.flashsale.FlashSaleSlotPort;
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
    private final FlashSaleSlotPort flashSaleSlotPort;

    @Transactional
    public void cancelInvalidRequest(Long checkoutId, String reason) {
        // Lỗi validate là thông tin kỹ thuật: chỉ lưu DB và log, người mua nhận câu cố định.
        cancel(checkoutId, Constant.CHECKOUT_INVALID_REQUEST, reason, Constant.CHECKOUT_INVALID_REQUEST_REASON);
    }

    // hàm đánh dấu cancel check out và payement nếu có kèm gửi event CheckoutFailed về notification
    @Transactional
    public void cancelCheckout(Long checkoutId, String reasonCode, String reason) {
        cancel(checkoutId, reasonCode, reason, reason);
    }

    private void cancel(Long checkoutId, String reasonCode, String reason, String displayReason) {
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
        reason = reason == null || reason.isBlank() ? Constant.CHECKOUT_DEFAULT_CANCEL_REASON : reason;
        displayReason = displayReason == null || displayReason.isBlank() ? reason : displayReason;
        checkout.cancel(reason, now);
        checkoutRepoPort.save(checkout);
        if (payment.isPresent() && payment.get().getStatus() == PaymentStatus.UNPAID) {
            payment.get().cancel(reason, now);
            paymentRepoPort.save(payment.get());
        }
        UUID eventId = UUID.randomUUID();
        CheckoutFailedPayload payload = new CheckoutFailedPayload(eventId, CheckoutFailedPayload.EVENT_TYPE,
                checkoutId, checkout.getStatus(), reasonCode, displayReason,
                now.atZone(clock.getZone()).toOffsetDateTime());
        outbox.save(new OutboxEventDto(eventId, Constant.CHECKOUT, checkoutId.toString(),
                payload.eventType(), payload, now));
        // Mọi đường hủy checkout chưa thành đơn đều qua đây, nên trả suất flash sale tại đây; DB đã được Product hoàn trước đó.
        flashSaleSlotPort.release(checkout.getIdempotencyKey());
        log.info("Đã ghi hủy checkout: checkout_id={}, reason_code={}, reason={}", checkoutId, reasonCode, reason);
    }
}
