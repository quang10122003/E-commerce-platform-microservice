package com.example.order_service.order.application.service;

import com.example.order_service.order.application.dto.CheckoutItemDto;
import com.example.order_service.order.application.dto.CheckoutItemKeyDto;
import com.example.order_service.order.application.dto.saga.CheckoutRequestedOutboxPayload;
import com.example.order_service.order.application.registry.CheckoutPaymentHandlerRegistry;
import com.example.order_service.order.application.port.out.repo.CheckoutRepoPort;
import com.example.order_service.order.domain.model.Checkout;
import com.example.order_service.order.domain.model.CheckoutStatus;
import com.example.order_service.order.domain.model.InboxEvent;
import com.example.order_service.cart.application.port.out.objettMapper.ObjectMapperPort;
import com.example.order_service.order.application.port.out.repo.InboxEventPort;
import org.springframework.transaction.annotation.Transactional;
import java.time.Clock;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@RequiredArgsConstructor
@Slf4j
public class CheckoutProcessingService {

    private final CheckoutPaymentHandlerRegistry paymentHandlerRegistry;
    private final CheckoutRepoPort checkoutRepoPort;
    private final CheckoutCancellationService checkoutCancellationService;
    private final InboxEventPort inboxEventPort;
    private final ObjectMapperPort json;
    private final Clock clock;

    @Transactional
    public void process(CheckoutRequestedOutboxPayload event) {
        if (event != null && event.eventId() != null && event.eventType() != null
                && !recordInboxEvent(event.eventId(), event.eventType(), event)) {
            return;
        }
        if (event == null || event.checkoutId() == null || event.checkoutId() <= 0) {
            log.warn("Bỏ qua event không có checkoutId hợp lệ: event_id={}, checkout_id={}",
                    event == null ? null : event.eventId(), event == null ? null : event.checkoutId());
            return;
        }
        Optional<Checkout> existing = checkoutRepoPort.findById(event.checkoutId());
        if (existing.isEmpty()) {
            log.warn("Bỏ qua event có checkout không tồn tại: event_id={}, checkout_id={}",
                    event.eventId(), event.checkoutId());
            return;
        }
        if (existing.get().getStatus() != CheckoutStatus.PROCESSING) {
            log.info("Bỏ qua event của checkout đã kết thúc: event_id={}, checkout_id={}, status={}",
                    event.eventId(), event.checkoutId(), existing.get().getStatus());
            return;
        }
        try {
            validateEvent(event);
        } catch (IllegalArgumentException exception) {
            checkoutCancellationService.cancelInvalidRequest(event.checkoutId(), exception.getMessage());
            return;
        }
        paymentHandlerRegistry.getHandler(event.paymentMethod()).handle(event);
    }

    // Ghi Inbox để tránh xử lý lặp; trả về false nếu eventId đã tồn tại.
    private boolean recordInboxEvent(UUID eventId, String eventType, Object payload) {
        InboxEvent inboxEvent = InboxEvent.create(eventId, eventType,
                json.writeValue(payload), LocalDateTime.now(clock));
        if (!inboxEventPort.tryInsert(inboxEvent)) {
            log.info("Bỏ qua event đã xử lý: event_id={}, event_type={}", eventId, eventType);
            return false;
        }
        return true;
    }

    private void validateEvent(CheckoutRequestedOutboxPayload event) {
        if (event == null || event.eventId() == null
                || !CheckoutRequestedOutboxPayload.EVENT_TYPE.equals(event.eventType())
                || event.checkoutId() == null || event.checkoutId() <= 0
                || event.userId() == null || event.userId() <= 0
                || event.paymentMethod() == null || event.occurredAt() == null
                || event.items() == null || event.items().isEmpty()) {
            throw new IllegalArgumentException("Event yêu cầu checkout không hợp lệ");
        }
        Set<CheckoutItemKeyDto> itemKeys = new HashSet<>();
        for (CheckoutItemDto item : event.items()) {
            if (item == null || item.variantId() == null || item.variantId() <= 0
                    || item.quantity() == null || item.quantity() <= 0 || item.sale() == null
                    || !itemKeys.add(new CheckoutItemKeyDto(item.variantId(), item.sale()))) {
                throw new IllegalArgumentException("Danh sách sản phẩm trong event checkout không hợp lệ");
            }
        }
    }
}
