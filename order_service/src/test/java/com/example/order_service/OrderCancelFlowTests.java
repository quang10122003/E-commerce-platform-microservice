package com.example.order_service;

import com.example.common.exception.BusinessException;
import com.example.order_service.application.constant.Constant;
import com.example.order_service.application.dto.event.CheckoutSucceededPayload;
import com.example.order_service.application.dto.outbox.OutboxEventDto;
import com.example.order_service.application.dto.response.UserInternalInfoResponse;
import com.example.order_service.application.dto.saga.CheckoutInventoryRollbackRequestedPayload;
import com.example.order_service.application.dto.saga.OrderInventoryRollbackRequestedPayload;
import com.example.order_service.application.error.OrderError;
import com.example.order_service.application.port.out.client.CurrentUserPort;
import com.example.order_service.application.port.out.outbox.OutboxPort;
import com.example.order_service.application.port.out.repo.CheckoutRepoPort;
import com.example.order_service.application.port.out.repo.OrderRepoPort;
import com.example.order_service.application.port.out.repo.PaymentRepoPort;
import com.example.order_service.application.service.PaymentOutcomeService;
import com.example.order_service.application.service.ShopAvailabilityService;
import com.example.order_service.application.service.UserOrderService;
import com.example.order_service.domain.error.OrderDomainError;
import com.example.order_service.domain.model.CheckoutPaymentMethod;
import com.example.order_service.domain.model.CheckoutStatus;
import com.example.order_service.domain.model.Order;
import com.example.order_service.domain.model.OrderCancelReason;
import com.example.order_service.domain.model.OrderCancelledBy;
import com.example.order_service.domain.model.OrderItem;
import com.example.order_service.domain.model.OrderStatus;
import com.example.order_service.domain.model.Payment;
import com.example.order_service.domain.model.PriceType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.mockito.ArgumentCaptor;

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

// Kiểm tra rule hủy đơn ở domain, API hủy của user và luồng hủy khi thanh toán thất bại.
class OrderCancelFlowTests {
    private final Clock clock = Clock.fixed(Instant.parse("2026-11-04T05:00:00Z"), ZoneId.of("Asia/Ho_Chi_Minh"));
    private final LocalDateTime now = LocalDateTime.now(clock);
    private final LocalDateTime createdAt = now.minusHours(1);
    private final CurrentUserPort currentUser = mock(CurrentUserPort.class);
    private final OrderRepoPort orders = mock(OrderRepoPort.class);
    private final OutboxPort outbox = mock(OutboxPort.class);
    private final PaymentRepoPort payments = mock(PaymentRepoPort.class);
    private final UserOrderService userOrders = new UserOrderService(currentUser, orders, mock(CheckoutRepoPort.class), mock(ShopAvailabilityService.class), outbox, clock);
    private final UUID checkoutSucceededId = UUID.randomUUID();
    private final UUID inventorySucceededId = UUID.randomUUID();

    // Dựng order 1 item với trạng thái, phương thức thanh toán và loại giá cần test.
    private Order order(OrderStatus status, CheckoutPaymentMethod method, PriceType priceType) {
        BigDecimal original = new BigDecimal("200000");
        BigDecimal unit = priceType == PriceType.SALE ? new BigDecimal("150000") : original;
        OrderItem item = OrderItem.reconstitute(11L, 21L, 31L, "Tai nghe", "[]", null, 2, original, unit,
                unit.multiply(BigDecimal.TWO), priceType, createdAt);
        boolean cancelled = status == OrderStatus.CANCEL;
        return Order.reconstitute(5L, 7L, 9L, "Shop A", null, priceType, method, status,
                unit.multiply(BigDecimal.TWO), cancelled ? OrderCancelledBy.SHOP : null, null,
                cancelled ? createdAt : null, null, null, null, createdAt, createdAt, List.of(item));
    }

    private void assertError(Object expected, Executable action) {
        BusinessException error = assertThrows(BusinessException.class, action);
        assertEquals(expected, error.getErrorCode());
    }

    private void loggedInAs(long userId) {
        when(currentUser.getInfo()).thenReturn(UserInternalInfoResponse.builder().userId(userId).build());
    }

    private void checkoutSucceededExists() {
        when(outbox.findCheckoutSucceeded(7L)).thenReturn(Optional.of(new CheckoutSucceededPayload(
                checkoutSucceededId, CheckoutSucceededPayload.EVENT_TYPE, 7L, 1L, CheckoutStatus.DONE,
                CheckoutPaymentMethod.COD, List.of(5L), new BigDecimal("400000"), now.atZone(clock.getZone())
                .toOffsetDateTime(), null, null, null, inventorySucceededId)));
    }

    @Test
    void userCancelsCodOrderBeforeShipping() {
        for (OrderStatus status : List.of(OrderStatus.PENDING_CONFIRMATION, OrderStatus.PENDING_PICKUP)) {
            Order order = order(status, CheckoutPaymentMethod.COD, PriceType.NORMAL);
            order.cancelByUser(OrderCancelReason.CHANGE_MIND, now);
            assertEquals(OrderStatus.CANCEL, order.getStatus());
            assertEquals(OrderCancelledBy.USER, order.getCancelledBy());
            assertEquals(OrderCancelReason.CHANGE_MIND, order.getCancelReason());
            assertEquals(now, order.getCancelledAt());
        }
    }

    @Test
    void userCannotCancelShippedOrDoneOrder() {
        for (OrderStatus status : List.of(OrderStatus.SHIPPING, OrderStatus.DONE)) {
            Order order = order(status, CheckoutPaymentMethod.COD, PriceType.NORMAL);
            assertError(OrderDomainError.INVALID_STATE, () -> order.cancelByUser(OrderCancelReason.OTHER, now));
            assertEquals(status, order.getStatus());
        }
    }

    @Test
    void userCannotCancelVnpayOrder() {
        Order order = order(OrderStatus.PENDING_CONFIRMATION, CheckoutPaymentMethod.VNPAY, PriceType.NORMAL);
        assertError(OrderDomainError.ONLINE_PAYMENT_NOT_CANCELLABLE,
                () -> order.cancelByUser(OrderCancelReason.CHANGE_MIND, now));
        assertEquals(OrderStatus.PENDING_CONFIRMATION, order.getStatus());
    }

    @Test
    void userMustChooseReason() {
        Order order = order(OrderStatus.PENDING_CONFIRMATION, CheckoutPaymentMethod.COD, PriceType.NORMAL);
        assertError(OrderDomainError.INVALID_DATA, () -> order.cancelByUser(null, now));
    }

    @Test
    void cancellingCancelledOrderKeepsOriginalActor() {
        Order order = order(OrderStatus.CANCEL, CheckoutPaymentMethod.COD, PriceType.NORMAL);
        order.cancelByUser(OrderCancelReason.CHANGE_MIND, now);
        assertEquals(OrderCancelledBy.SHOP, order.getCancelledBy());
        assertNull(order.getCancelReason());
    }

    @Test
    void cancelOrderPublishesRollbackForThatOrderOnly() {
        loggedInAs(1L);
        checkoutSucceededExists();
        Order order = order(OrderStatus.PENDING_PICKUP, CheckoutPaymentMethod.COD, PriceType.SALE);
        when(orders.findUserOrderForUpdate(5L, 1L)).thenReturn(Optional.of(order));

        userOrders.cancelMyOrder(5L, OrderCancelReason.WRONG_ADDRESS);

        verify(orders).saveStatus(List.of(order));
        assertEquals(OrderCancelReason.WRONG_ADDRESS, order.getCancelReason());
        ArgumentCaptor<OutboxEventDto> saved = ArgumentCaptor.forClass(OutboxEventDto.class);
        verify(outbox).save(saved.capture());
        assertEquals(Constant.ORDER, saved.getValue().aggregateType());
        assertEquals("5", saved.getValue().aggregateId());
        OrderInventoryRollbackRequestedPayload event = (OrderInventoryRollbackRequestedPayload) saved.getValue().payload();
        assertEquals(OrderInventoryRollbackRequestedPayload.EVENT_TYPE, event.eventType());
        assertEquals(7L, event.checkoutId());
        assertEquals(5L, event.orderId());
        assertEquals(9L, event.shopId());
        assertTrue(event.sale());
        assertEquals(inventorySucceededId, event.inventorySucceededEventId());
        assertEquals(checkoutSucceededId, event.checkoutSucceededEventId());
    }

    @Test
    void orderOfAnotherUserIsNotFound() {
        loggedInAs(2L);
        when(orders.findUserOrderForUpdate(5L, 2L)).thenReturn(Optional.empty());
        assertError(OrderError.ORDER_NOT_FOUND, () -> userOrders.cancelMyOrder(5L, OrderCancelReason.OTHER));
        verify(orders, never()).saveStatus(any());
        verifyNoInteractions(outbox);
    }

    @Test
    void repeatedCancelDoesNotRollbackAgain() {
        loggedInAs(1L);
        when(orders.findUserOrderForUpdate(5L, 1L))
                .thenReturn(Optional.of(order(OrderStatus.CANCEL, CheckoutPaymentMethod.COD, PriceType.NORMAL)));
        userOrders.cancelMyOrder(5L, OrderCancelReason.CHANGE_MIND);
        verify(orders, never()).saveStatus(any());
        verifyNoInteractions(outbox);
    }

    @Test
    void rejectedCancelWritesNothing() {
        loggedInAs(1L);
        when(orders.findUserOrderForUpdate(5L, 1L))
                .thenReturn(Optional.of(order(OrderStatus.PENDING_CONFIRMATION, CheckoutPaymentMethod.VNPAY, PriceType.NORMAL)));
        assertError(OrderDomainError.ONLINE_PAYMENT_NOT_CANCELLABLE,
                () -> userOrders.cancelMyOrder(5L, OrderCancelReason.CHANGE_MIND));
        verify(orders, never()).saveStatus(any());
        verifyNoInteractions(outbox);
    }

    @Test
    void missingCheckoutSucceededFailsSoTransactionRollsBack() {
        loggedInAs(1L);
        when(orders.findUserOrderForUpdate(5L, 1L))
                .thenReturn(Optional.of(order(OrderStatus.PENDING_CONFIRMATION, CheckoutPaymentMethod.COD, PriceType.NORMAL)));
        when(outbox.findCheckoutSucceeded(7L)).thenReturn(Optional.empty());
        assertThrows(IllegalStateException.class, () -> userOrders.cancelMyOrder(5L, OrderCancelReason.OTHER));
        verify(outbox, never()).save(any());
    }

    @Test
    void failedPaymentCancelsAllOrdersOfCheckoutWithoutReason() {
        checkoutSucceededExists();
        Payment payment = Payment.create(7L, new BigDecimal("400000"), createdAt);
        payment.prepare(new BigDecimal("400000"), "ref-1", now.plusMinutes(15), createdAt);
        payment.start("https://sandbox.vnpayment.vn/pay", createdAt);
        Order order = order(OrderStatus.PENDING_CONFIRMATION, CheckoutPaymentMethod.VNPAY, PriceType.NORMAL);
        when(orders.findByCheckoutIdForUpdate(7L)).thenReturn(List.of(order));

        new PaymentOutcomeService(payments, orders, outbox, clock)
                .cancelPaymentAndOrders(payment, "VNPay không ghi nhận", OrderCancelledBy.SYSTEM, now);

        assertEquals(OrderStatus.CANCEL, order.getStatus());
        assertEquals(OrderCancelledBy.SYSTEM, order.getCancelledBy());
        assertNull(order.getCancelReason());
        ArgumentCaptor<OutboxEventDto> saved = ArgumentCaptor.forClass(OutboxEventDto.class);
        verify(outbox).save(saved.capture());
        assertInstanceOf(CheckoutInventoryRollbackRequestedPayload.class, saved.getValue().payload());
    }
}
