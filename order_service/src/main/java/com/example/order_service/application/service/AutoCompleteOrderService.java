package com.example.order_service.application.service;

import com.example.order_service.application.constant.Constant;
import com.example.order_service.application.dto.event.OrderStatusChangedPayload;
import com.example.order_service.application.dto.outbox.OutboxEventDto;
import com.example.order_service.application.port.in.AutoCompleteOrderUseCase;
import com.example.order_service.application.port.out.outbox.OutboxPort;
import com.example.order_service.application.port.out.repo.CheckoutRepoPort;
import com.example.order_service.application.port.out.repo.OrderRepoPort;
import com.example.order_service.domain.model.Checkout;
import com.example.order_service.domain.model.Order;
import com.example.order_service.domain.model.OrderStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

// Tự chuyển đơn đang giao sang hoàn thành khi đã giao cho đơn vị vận chuyển đủ thời gian cấu hình.
@Slf4j
@RequiredArgsConstructor
public class AutoCompleteOrderService implements AutoCompleteOrderUseCase {

    private static final int BATCH_SIZE = 100;

    private final OrderRepoPort orderRepoPort;
    private final CheckoutRepoPort checkoutRepoPort;
    private final OutboxPort outboxPort;
    private final Clock clock;
    private final Duration completeAfter;

    @Override
    @Transactional(readOnly = true)
    public List<Long> findDueOrderIds() {
        return orderRepoPort.findShippingOrderIdsDueBefore(LocalDateTime.now(clock).minus(completeAfter), BATCH_SIZE);
    }

    // Mỗi đơn một transaction có khóa dòng; kiểm tra lại điều kiện vì đơn có thể đã đổi sau lúc quét.
    @Override
    @Transactional
    public void complete(Long orderId) {
        Optional<Order> found = orderRepoPort.findByIdForUpdate(orderId);
        if (found.isEmpty() || found.get().getStatus() != OrderStatus.SHIPPING) {
            log.info("Bỏ qua tự hoàn thành đơn không còn đang giao: order_id={}", orderId);
            return;
        }
        Order order = found.get();
        LocalDateTime now = LocalDateTime.now(clock);
        LocalDateTime shippedAt = order.getShippedAt() != null ? order.getShippedAt() : order.getUpdatedAt();
        if (shippedAt.plus(completeAfter).isAfter(now)) {
            return;
        }
        order.complete(now);
        orderRepoPort.saveStatus(List.of(order));
        // Ghi event cùng transaction để Notification gửi mail "đã giao thành công" như khi shop đổi trạng thái.
        Checkout checkout = checkoutRepoPort.findById(order.getCheckoutId())
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy checkout của đơn hàng"));
        OrderStatusChangedPayload event = OrderStatusChangedPayload.of(order, checkout, clock.getZone());
        outboxPort.save(new OutboxEventDto(event.eventId(), Constant.ORDER, orderId.toString(), event.eventType(), event, now));
        log.info("Đã tự hoàn thành đơn đang giao: order_id={}, shipped_at={}, event_id={}", orderId, shippedAt, event.eventId());
    }
}
