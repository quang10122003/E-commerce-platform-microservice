package com.example.order_service.order.application.service;

import com.example.order_service.cart.application.dto.outbox.OutboxEventDto;
import com.example.order_service.cart.application.dto.repone.UserInternaInfoRespone;
import com.example.order_service.cart.application.port.out.outbox.OutboxPort;
import com.example.order_service.order.application.Constant.Constant;
import com.example.order_service.order.application.dto.saga.CheckoutRequestedOutboxPayload;
import com.example.order_service.order.application.dto.request.CreateCheckoutRequest;
import com.example.order_service.order.application.port.out.repo.CheckoutRepoPort;
import com.example.order_service.order.application.port.out.repo.PaymentRepoPort;
import com.example.order_service.order.domain.model.Checkout;
import com.example.order_service.order.domain.model.CheckoutPaymentMethod;
import com.example.order_service.order.domain.model.Payment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.UUID;

@RequiredArgsConstructor
@Slf4j
public class CheckoutCreationService {

    private final CheckoutRepoPort checkoutRepoPort;
    private final PaymentRepoPort paymentRepoPort;
    private final OutboxPort outboxPort;
    private final Clock clock;

    @Transactional
    public Checkout create(CreateCheckoutRequest request, String idempotencyKey, String clientIp,
                           UserInternaInfoRespone buyer) {
        LocalDateTime now = LocalDateTime.now(clock);
        Checkout checkout = Checkout.create(buyer.userId(), buyer.fullName(), buyer.email(),
                idempotencyKey, request.shippingAddress(), clientIp, request.note(), request.totalAmount(), now);
        Checkout saved = checkoutRepoPort.save(checkout);
        if (request.paymentMethod() == CheckoutPaymentMethod.VNPAY) {
            paymentRepoPort.save(Payment.create(saved.getId(), now));
        }

        UUID eventId = UUID.randomUUID();
        CheckoutRequestedOutboxPayload payload = new CheckoutRequestedOutboxPayload(eventId,
                CheckoutRequestedOutboxPayload.EVENT_TYPE, saved.getId(), buyer.userId(),
                request.paymentMethod(), request.items(), now.atZone(clock.getZone()).toOffsetDateTime());
        outboxPort.save(new OutboxEventDto(eventId, Constant.CHECKOUT,
                saved.getId().toString(), CheckoutRequestedOutboxPayload.EVENT_TYPE, payload, now));
        log.info("Đã ghi yêu cầu checkout: checkout_id={}, event_id={}, event_type={}, item_count={}",
                saved.getId(), eventId, payload.eventType(), payload.items().size());
        return saved;
    }
}
