package com.example.order_service.application.service;

import com.example.order_service.application.constant.Constant;
import com.example.order_service.application.dto.event.CheckoutSucceededPayload;
import com.example.order_service.application.dto.outbox.OutboxEventDto;
import com.example.order_service.application.dto.saga.CheckoutInventoryRollbackRequestedPayload;
import com.example.order_service.application.port.out.outbox.OutboxPort;
import com.example.order_service.application.port.out.repo.OrderRepoPort;
import com.example.order_service.application.port.out.repo.PaymentRepoPort;
import com.example.order_service.domain.model.Order;
import com.example.order_service.domain.model.OrderCancelledBy;
import com.example.order_service.domain.model.Payment;
import com.example.order_service.domain.model.PaymentStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

// Caller phải đang giữ khóa dòng payment để IPN và job đối soát không cập nhật chồng lên nhau.
@Slf4j
@RequiredArgsConstructor
public class PaymentOutcomeService {

    private final PaymentRepoPort paymentRepoPort;
    private final OrderRepoPort orderRepoPort;
    private final OutboxPort outboxPort;
    private final Clock clock;

    @Transactional
    public void savePaidAndConfirmOrders(Payment payment, LocalDateTime now) {
        if (payment.getStatus() != PaymentStatus.DONE) {
            throw new IllegalStateException("Payment chưa được đánh dấu đã thanh toán");
        }
        paymentRepoPort.save(payment);
        List<Order> orders = orderRepoPort.findByCheckoutIdForUpdate(payment.getCheckoutId());
        orders.forEach((Order order) -> order.confirm(now));
        orderRepoPort.saveStatus(orders);
        log.info("Đã xác nhận thanh toán và chuyển order sang chờ lấy hàng: payment_id={}, checkout_id={}, "
                + "order_count={}", payment.getId(), payment.getCheckoutId(), orders.size());
    }

    @Transactional
    public void cancelPaymentAndOrders(Payment payment, String reason, OrderCancelledBy cancelledBy, LocalDateTime now) {
        payment.cancelPending(reason, now);
        paymentRepoPort.save(payment);
        List<Order> orders = orderRepoPort.findByCheckoutIdForUpdate(payment.getCheckoutId());
        orders.forEach((Order order) -> order.cancel(cancelledBy, null, now));
        orderRepoPort.saveStatus(orders);
        UUID rollbackEventId = requestInventoryRollback(payment.getCheckoutId(), reason, now);
        log.info("Đã hủy payment, order và yêu cầu hoàn tồn kho: payment_id={}, checkout_id={}, order_count={}, "
                + "event_id={}", payment.getId(), payment.getCheckoutId(), orders.size(), rollbackEventId);
    }

    // Product dùng hai event id này để tìm lượng đã trừ tồn và biết lượt bán đã được cộng hay chưa.
    private UUID requestInventoryRollback(Long checkoutId, String reason, LocalDateTime now) {
        CheckoutSucceededPayload succeeded = outboxPort.findCheckoutSucceeded(checkoutId)
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy event CHECKOUT_SUCCEEDED của checkout"));
        UUID eventId = UUID.randomUUID();
        CheckoutInventoryRollbackRequestedPayload event = new CheckoutInventoryRollbackRequestedPayload(eventId,
                CheckoutInventoryRollbackRequestedPayload.EVENT_TYPE, checkoutId,
                succeeded.inventorySucceededEventId(), succeeded.eventId(), Constant.CHECKOUT_PAYMENT_FAILED,
                reason, now.atZone(clock.getZone()).toOffsetDateTime());
        outboxPort.save(new OutboxEventDto(eventId, Constant.CHECKOUT, checkoutId.toString(),
                event.eventType(), event, now));
        return eventId;
    }
}
