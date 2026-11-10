package com.example.order_service;

import com.example.order_service.adapter.out.payment.VnpayPaymentAdapter;
import com.example.order_service.application.dto.checkout.CheckoutItemSnapshotDto;
import com.example.order_service.application.dto.checkout.ShopSnapshotDto;
import com.example.order_service.application.dto.event.CheckoutSucceededPayload;
import com.example.order_service.application.dto.outbox.OutboxEventDto;
import com.example.order_service.application.dto.response.PaymentUrlResponse;
import com.example.order_service.application.dto.saga.CheckoutInventorySucceededPayload;
import com.example.order_service.application.dto.saga.CheckoutInventoryRollbackRequestedPayload;
import com.example.order_service.application.strategy.eventKafka.CheckoutInventorySucceededHandler;
import com.example.order_service.application.constant.Constant;
import com.example.order_service.application.port.out.client.ShopSnapshotPort;
import com.example.order_service.application.port.out.outbox.OutboxPort;
import com.example.order_service.application.port.out.repo.CheckoutRepoPort;
import com.example.order_service.application.port.out.repo.InboxEventPort;
import com.example.order_service.application.port.out.repo.OrderRepoPort;
import com.example.order_service.application.port.out.repo.PaymentRepoPort;
import com.example.order_service.application.port.out.serialization.ObjectMapperPort;
import com.example.order_service.application.service.CheckoutService;
import com.example.order_service.application.service.VnpayPaymentService;
import com.example.order_service.domain.model.Checkout;
import com.example.order_service.domain.model.CheckoutPaymentMethod;
import com.example.order_service.domain.model.CheckoutStatus;
import com.example.order_service.domain.model.Order;
import com.example.order_service.domain.model.Payment;
import com.example.order_service.domain.model.PaymentStatus;
import com.example.order_service.infrastructure.config.VnpayProperties;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;
import org.springframework.transaction.support.SimpleTransactionStatus;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class VnpayPaymentFlowTests {
    private final Clock clock = Clock.fixed(Instant.parse("2026-11-04T05:00:00Z"), ZoneId.of("Asia/Ho_Chi_Minh"));
    private final LocalDateTime now = LocalDateTime.now(clock);
    private final BigDecimal amount = new BigDecimal("10000");
    private final CheckoutRepoPort checkouts = mock(CheckoutRepoPort.class);
    private final PaymentRepoPort payments = mock(PaymentRepoPort.class);
    private final OrderRepoPort orders = mock(OrderRepoPort.class);
    private final InboxEventPort inbox = mock(InboxEventPort.class);
    private final OutboxPort outbox = mock(OutboxPort.class);
    private final ShopSnapshotPort shops = mock(ShopSnapshotPort.class);
    private final ObjectMapperPort json = mock(ObjectMapperPort.class);

    private VnpayPaymentAdapter adapter() {
        return new VnpayPaymentAdapter(new VnpayProperties(
                "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html", "DEMOV210", "test-secret",
                "https://shop.example/payment?source=vnpay&lang=vn", "2.1.0", "pay", "VND", "vn", "other", 15));
    }

    @Test
    void signsEncodedSortedParametersAndRejectsInvalidAmounts() {
        PaymentUrlResponse result = adapter().createPaymentUrl("ABC123", amount, "127.0.0.1", now);
        assertEquals(now.plusMinutes(15), result.expiresAt());
        assertTrue(result.url().contains("vnp_Amount=1000000&vnp_Command=pay&vnp_CreateDate=20261104120000"));
        assertTrue(result.url().contains("vnp_ExpireDate=20261104121500"));
        assertTrue(result.url().endsWith("vnp_SecureHash="
                + "19fad8eb86ae32ca4dde5ad70846d6d1d0bd9fe193de56296e1c0167c3587d9a2"
                + "15257637061e8df6828605c7a90c944ba5455a4d7f03921c32c043272a8f79e"));
        for (BigDecimal invalid : List.of(BigDecimal.ZERO, new BigDecimal("1.5"), new BigDecimal("10000000000"))) {
            assertThrows(IllegalArgumentException.class,
                    () -> adapter().createPaymentUrl("ABC123", invalid, "127.0.0.1", now));
        }
    }

    @Test
    void createsOrdersBeforePaymentAndPublishesUrlOnlyOnce() {
        Checkout checkout = checkout();
        Payment payment = Payment.create(1L, amount, now);
        prepare(checkout, Optional.of(payment));
        CheckoutService service = service(new VnpayPaymentService(adapter()::createPaymentUrl, payments));
        CheckoutInventorySucceededPayload inventoryEvent = event();
        service.processInventorySucceeded(inventoryEvent);

        InOrder sequence = inOrder(orders, payments, checkouts, outbox);
        sequence.verify(payments).findByCheckoutIdForUpdate(1L);
        sequence.verify(orders).saveAll(anyList());
        sequence.verify(payments).save(payment);
        sequence.verify(checkouts).save(checkout);
        sequence.verify(outbox).save(any());
        assertEquals(PaymentStatus.PENDING, payment.getStatus());
        assertEquals(now.plusMinutes(15), payment.getExpiresAt());
        ArgumentCaptor<OutboxEventDto> published = ArgumentCaptor.forClass(OutboxEventDto.class);
        verify(outbox).save(published.capture());
        CheckoutSucceededPayload payload = (CheckoutSucceededPayload) published.getValue().payload();
        assertEquals(CheckoutPaymentMethod.VNPAY, payload.paymentMethod());
        assertOrdersPaymentMethod(CheckoutPaymentMethod.VNPAY);
        assertEquals(inventoryEvent.eventId(), payload.inventorySucceededEventId());
        assertEquals(payment.getPaymentUrl(), payload.urlPayment());
        service.processInventorySucceeded(event());
        verify(orders, times(1)).saveAll(anyList());
        verify(payments, times(1)).save(payment);
    }

    @Test
    void codCompletesWithoutCallingPaymentGateway() {
        prepare(checkout(), Optional.empty());
        VnpayPaymentService gateway = mock(VnpayPaymentService.class);
        CheckoutInventorySucceededPayload inventoryEvent = event();
        service(gateway).processInventorySucceeded(inventoryEvent);
        verifyNoInteractions(gateway);
        ArgumentCaptor<OutboxEventDto> published = ArgumentCaptor.forClass(OutboxEventDto.class);
        verify(outbox).save(published.capture());
        CheckoutSucceededPayload payload = (CheckoutSucceededPayload) published.getValue().payload();
        assertEquals(CheckoutPaymentMethod.COD, payload.paymentMethod());
        assertOrdersPaymentMethod(CheckoutPaymentMethod.COD);
        assertEquals(inventoryEvent.eventId(), payload.inventorySucceededEventId());
        assertNull(payload.urlPayment());
    }

    @Test
    void gatewayFailureRollsBackTransactionAndDoesNotPublishSuccess() {
        prepare(checkout(), Optional.of(Payment.create(1L, amount, now)));
        VnpayPaymentService gateway = new VnpayPaymentService((String reference, BigDecimal total,
                String ip, LocalDateTime createdAt) -> { throw new IllegalStateException("Lỗi ký URL"); }, payments);
        PlatformTransactionManager transactions = mock(PlatformTransactionManager.class);
        SimpleTransactionStatus transaction = new SimpleTransactionStatus();
        when(transactions.getTransaction(any())).thenReturn(transaction);
        ProxyFactory factory = new ProxyFactory(service(gateway));
        factory.setProxyTargetClass(true);
        TransactionInterceptor interceptor = new TransactionInterceptor();
        interceptor.setTransactionManager(transactions);
        interceptor.setTransactionAttributeSource(new AnnotationTransactionAttributeSource());
        factory.addAdvice(interceptor);
        CheckoutService proxied = (CheckoutService) factory.getProxy();

        assertThrows(IllegalStateException.class, () -> proxied.processInventorySucceeded(event()));
        verify(transactions).rollback(transaction);
        verify(transactions, never()).commit(any());
        verify(orders).saveAll(anyList());
        verify(payments, never()).save(any());
        verify(checkouts, never()).save(any());
        verify(outbox, never()).save(any());
    }

    private Checkout checkout() {
        return Checkout.reconstitute(1L, 2L, "Người mua", "buyer@example.com", UUID.randomUUID().toString(),
                "Hà Nội", "127.0.0.1", null, amount, CheckoutStatus.PROCESSING, null, now, now);
    }

    @Test
    void handlerRequestsCompensationAfterCompletionFailure() {
        CheckoutService checkoutService = mock(CheckoutService.class);
        CheckoutInventorySucceededPayload payload = event();
        IllegalStateException failure = new IllegalStateException("Lỗi tạo URL");
        when(json.readValue("event", CheckoutInventorySucceededPayload.class)).thenReturn(payload);
        doThrow(failure).when(checkoutService).processInventorySucceeded(payload);

        new CheckoutInventorySucceededHandler(json, checkoutService).handle("event");

        InOrder sequence = inOrder(checkoutService);
        sequence.verify(checkoutService).processInventorySucceeded(payload);
        sequence.verify(checkoutService).requestInventoryRollback(payload, failure);
    }

    @Test
    void failedCompletionPublishesRollbackOnceWithoutLeakingTechnicalError() {
        prepare(checkout(), Optional.empty());
        CheckoutInventorySucceededPayload payload = event();
        CheckoutService checkoutService = service(mock(VnpayPaymentService.class));
        IllegalStateException failure = new IllegalStateException("Chi tiết kết nối nhạy cảm");

        checkoutService.requestInventoryRollback(payload, failure);
        when(inbox.tryInsert(any())).thenReturn(false);
        checkoutService.requestInventoryRollback(payload, failure);

        ArgumentCaptor<OutboxEventDto> published = ArgumentCaptor.forClass(OutboxEventDto.class);
        verify(outbox, times(1)).save(published.capture());
        CheckoutInventoryRollbackRequestedPayload rollback =
                (CheckoutInventoryRollbackRequestedPayload) published.getValue().payload();
        assertEquals(payload.eventId(), rollback.inventorySucceededEventId());
        assertEquals(payload.checkoutId(), rollback.checkoutId());
        assertEquals(Constant.CHECKOUT_ORDER_CREATION_FAILED, rollback.reasonCode());
        assertFalse(rollback.reason().contains(failure.getMessage()));
        verifyNoInteractions(orders);
    }

    @Test
    void compensationWriteFailurePropagatesForKafkaRetry() {
        CheckoutService checkoutService = mock(CheckoutService.class);
        CheckoutInventorySucceededPayload payload = event();
        IllegalStateException failure = new IllegalStateException("Lỗi ghi đơn");
        IllegalStateException recoveryFailure = new IllegalStateException("Lỗi ghi outbox");
        when(json.readValue("event", CheckoutInventorySucceededPayload.class)).thenReturn(payload);
        doThrow(failure).when(checkoutService).processInventorySucceeded(payload);
        doThrow(recoveryFailure).when(checkoutService).requestInventoryRollback(payload, failure);

        assertSame(recoveryFailure, assertThrows(IllegalStateException.class,
                () -> new CheckoutInventorySucceededHandler(json, checkoutService).handle("event")));
    }

    @SuppressWarnings("unchecked")
    private void assertOrdersPaymentMethod(CheckoutPaymentMethod expected) {
        ArgumentCaptor<List<Order>> saved = ArgumentCaptor.forClass(List.class);
        verify(orders, atLeastOnce()).saveAll(saved.capture());
        assertFalse(saved.getValue().isEmpty());
        saved.getValue().forEach((Order order) -> assertEquals(expected, order.getPaymentMethod()));
    }

    private void prepare(Checkout checkout, Optional<Payment> payment) {
        when(json.writeValue(any())).thenReturn("{}");
        when(inbox.tryInsert(any())).thenReturn(true);
        when(checkouts.findByIdForUpdate(1L)).thenReturn(Optional.of(checkout));
        when(payments.findByCheckoutIdForUpdate(1L)).thenReturn(payment);
        when(shops.findById(3L)).thenReturn(Optional.of(new ShopSnapshotDto(3L, "Cửa hàng", null)));
        when(orders.saveAll(anyList())).thenReturn(List.of(4L));
    }

    private CheckoutService service(VnpayPaymentService gateway) {
        return new CheckoutService(null, checkouts, null, inbox, outbox, orders, shops, json, null,
                payments, gateway, clock);
    }

    private CheckoutInventorySucceededPayload event() {
        return new CheckoutInventorySucceededPayload(UUID.randomUUID(), CheckoutInventorySucceededPayload.EVENT_TYPE,
                1L, UUID.randomUUID(), List.of(new CheckoutItemSnapshotDto(5L, 6L, 3L, "Sản phẩm", "{}",
                null, 1, amount, amount, false, null)), amount, now.atZone(clock.getZone()).toOffsetDateTime());
    }
}
