package com.example.order_service;

import com.example.common.exception.BusinessException;
import com.example.order_service.application.dto.event.CheckoutSucceededPayload;
import com.example.order_service.application.dto.event.OrderCancelledByShopPayload;
import com.example.order_service.application.dto.outbox.OutboxEventDto;
import com.example.order_service.application.dto.response.UserInternalInfoResponse;
import com.example.order_service.application.dto.saga.OrderInventoryRollbackRequestedPayload;
import com.example.order_service.application.error.OrderError;
import com.example.order_service.application.port.out.client.CurrentUserPort;
import com.example.order_service.application.port.out.outbox.OutboxPort;
import com.example.order_service.application.port.out.repo.CheckoutRepoPort;
import com.example.order_service.application.port.out.repo.OrderRepoPort;
import com.example.order_service.application.service.ShopAvailabilityService;
import com.example.order_service.application.service.UserOrderService;
import com.example.order_service.domain.error.OrderDomainError;
import com.example.order_service.domain.model.Checkout;
import com.example.order_service.domain.model.CheckoutPaymentMethod;
import com.example.order_service.domain.model.CheckoutStatus;
import com.example.order_service.domain.model.Order;
import com.example.order_service.domain.model.OrderCancelReason;
import com.example.order_service.domain.model.OrderCancelledBy;
import com.example.order_service.domain.model.OrderItem;
import com.example.order_service.domain.model.OrderStatus;
import com.example.order_service.domain.model.PriceType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

// Kiểm tra rule shop hủy đơn, rule lý do theo người hủy và hai event phát ra (hoàn kho + gửi mail).
class ShopOrderCancelTests {
    private final Clock clock = Clock.fixed(Instant.parse("2026-11-04T05:00:00Z"), ZoneId.of("Asia/Ho_Chi_Minh"));
    private final LocalDateTime now = LocalDateTime.now(clock);
    private final LocalDateTime createdAt = now.minusHours(1);
    private final CurrentUserPort currentUser = mock(CurrentUserPort.class);
    private final OrderRepoPort orders = mock(OrderRepoPort.class);
    private final CheckoutRepoPort checkouts = mock(CheckoutRepoPort.class);
    private final ShopAvailabilityService shops = mock(ShopAvailabilityService.class);
    private final OutboxPort outbox = mock(OutboxPort.class);
    private final UserOrderService service = new UserOrderService(currentUser, orders, checkouts, shops, outbox, clock);

    private Order order(OrderStatus status, CheckoutPaymentMethod method) {
        BigDecimal price = new BigDecimal("150000");
        OrderItem item = OrderItem.reconstitute(11L, 21L, 31L, "Tai nghe", "[]", null, 2, price, price,
                price.multiply(BigDecimal.TWO), PriceType.NORMAL, createdAt);
        boolean cancelled = status == OrderStatus.CANCEL;
        return Order.reconstitute(5L, 7L, 9L, "Shop A", null, PriceType.NORMAL, method, status,
                price.multiply(BigDecimal.TWO), cancelled ? OrderCancelledBy.USER : null,
                cancelled ? OrderCancelReason.CHANGE_MIND : null, cancelled ? createdAt : null, null, null, null, createdAt, createdAt,
                List.of(item));
    }

    private void assertError(Object expected, Executable action) {
        assertEquals(expected, assertThrows(BusinessException.class, action).getErrorCode());
    }

    private void shopLoggedIn(Boolean locked) {
        when(currentUser.getInfo()).thenReturn(UserInternalInfoResponse.builder().userId(9L).build());
        when(shops.getShopLocks(List.of(9L))).thenReturn(locked == null ? Map.of() : Map.of(9L, locked));
    }

    private void checkoutDataExists() {
        when(outbox.findCheckoutSucceeded(7L)).thenReturn(Optional.of(new CheckoutSucceededPayload(UUID.randomUUID(),
                CheckoutSucceededPayload.EVENT_TYPE, 7L, 1L, CheckoutStatus.DONE, CheckoutPaymentMethod.COD,
                List.of(5L), new BigDecimal("300000"), OffsetDateTime.now(clock), null, null, null, UUID.randomUUID())));
        when(checkouts.findById(7L)).thenReturn(Optional.of(Checkout.reconstitute(7L, 1L, "Nguyễn Văn A",
                "buyer@example.com", UUID.randomUUID().toString(), "1 Lê Lợi", "127.0.0.1", null,
                new BigDecimal("300000"), CheckoutStatus.DONE, null, createdAt, createdAt)));
    }

    @Test
    void reasonsAreSplitByActor() {
        assertTrue(OrderCancelReason.OUT_OF_STOCK.allowedFor(OrderCancelledBy.SHOP));
        assertFalse(OrderCancelReason.OUT_OF_STOCK.allowedFor(OrderCancelledBy.USER));
        assertTrue(OrderCancelReason.CHANGE_MIND.allowedFor(OrderCancelledBy.USER));
        assertFalse(OrderCancelReason.CHANGE_MIND.allowedFor(OrderCancelledBy.SHOP));
        assertTrue(OrderCancelReason.OTHER.allowedFor(OrderCancelledBy.USER));
        assertTrue(OrderCancelReason.OTHER.allowedFor(OrderCancelledBy.SHOP));
        assertFalse(OrderCancelReason.OTHER.allowedFor(OrderCancelledBy.SYSTEM));
    }

    @Test
    void shopCancelsCodOrderBeforeShippingWithShopReason() {
        for (OrderStatus status : List.of(OrderStatus.PENDING_CONFIRMATION, OrderStatus.PENDING_PICKUP)) {
            Order order = order(status, CheckoutPaymentMethod.COD);
            order.cancelByShop(OrderCancelReason.OUT_OF_STOCK, now);
            assertEquals(OrderStatus.CANCEL, order.getStatus());
            assertEquals(OrderCancelledBy.SHOP, order.getCancelledBy());
            assertEquals(OrderCancelReason.OUT_OF_STOCK, order.getCancelReason());
        }
    }

    @Test
    void wrongActorReasonIsRejectedForBothSides() {
        Order shopSide = order(OrderStatus.PENDING_CONFIRMATION, CheckoutPaymentMethod.COD);
        assertError(OrderDomainError.CANCEL_REASON_NOT_ALLOWED, () -> shopSide.cancelByShop(OrderCancelReason.CHANGE_MIND, now));
        Order userSide = order(OrderStatus.PENDING_CONFIRMATION, CheckoutPaymentMethod.COD);
        assertError(OrderDomainError.CANCEL_REASON_NOT_ALLOWED, () -> userSide.cancelByUser(OrderCancelReason.OUT_OF_STOCK, now));
        assertEquals(OrderStatus.PENDING_CONFIRMATION, userSide.getStatus());
    }

    @Test
    void shopCannotCancelVnpayOrShippedOrder() {
        assertError(OrderDomainError.ONLINE_PAYMENT_NOT_CANCELLABLE, () -> order(OrderStatus.PENDING_CONFIRMATION,
                CheckoutPaymentMethod.VNPAY).cancelByShop(OrderCancelReason.OUT_OF_STOCK, now));
        assertError(OrderDomainError.INVALID_STATE, () -> order(OrderStatus.SHIPPING, CheckoutPaymentMethod.COD)
                .cancelByShop(OrderCancelReason.OUT_OF_STOCK, now));
    }

    @Test
    void lockedOrUnknownShopIsRejectedBeforeLockingOrder() {
        for (Boolean locked : new Boolean[] {true, null}) {
            reset(orders, shops, currentUser);
            shopLoggedIn(locked);
            assertError(OrderError.SHOP_LOCKED, () -> service.cancelShopOrder(5L, OrderCancelReason.OUT_OF_STOCK));
            verifyNoInteractions(orders);
        }
    }

    @Test
    void orderOfAnotherShopIsNotFound() {
        shopLoggedIn(false);
        when(orders.findShopOrderForUpdate(5L, 9L)).thenReturn(Optional.empty());
        assertError(OrderError.ORDER_NOT_FOUND, () -> service.cancelShopOrder(5L, OrderCancelReason.OUT_OF_STOCK));
        verifyNoInteractions(outbox);
    }

    @Test
    void alreadyCancelledOrderKeepsOriginalCancellation() {
        shopLoggedIn(false);
        Order order = order(OrderStatus.CANCEL, CheckoutPaymentMethod.COD);
        when(orders.findShopOrderForUpdate(5L, 9L)).thenReturn(Optional.of(order));
        service.cancelShopOrder(5L, OrderCancelReason.OUT_OF_STOCK);
        assertEquals(OrderCancelledBy.USER, order.getCancelledBy());
        verify(orders, never()).saveStatus(any());
        verifyNoInteractions(outbox);
    }

    @Test
    void cancelPublishesRollbackAndBuyerMailEvents() {
        shopLoggedIn(false);
        checkoutDataExists();
        Order order = order(OrderStatus.PENDING_PICKUP, CheckoutPaymentMethod.COD);
        when(orders.findShopOrderForUpdate(5L, 9L)).thenReturn(Optional.of(order));

        service.cancelShopOrder(5L, OrderCancelReason.CANNOT_DELIVER);

        verify(orders).saveStatus(List.of(order));
        ArgumentCaptor<OutboxEventDto> saved = ArgumentCaptor.forClass(OutboxEventDto.class);
        verify(outbox, times(2)).save(saved.capture());
        assertInstanceOf(OrderInventoryRollbackRequestedPayload.class, saved.getAllValues().get(0).payload());
        OrderCancelledByShopPayload mail = (OrderCancelledByShopPayload) saved.getAllValues().get(1).payload();
        assertEquals(OrderCancelledByShopPayload.EVENT_TYPE, saved.getAllValues().get(1).eventType());
        assertEquals(5L, mail.orderId());
        assertEquals("Shop A", mail.shopName());
        assertEquals("buyer@example.com", mail.buyerEmail());
        assertEquals("Nguyễn Văn A", mail.buyerFullName());
        assertEquals(OrderCancelReason.CANNOT_DELIVER, mail.cancelReason());
        assertEquals(0, new BigDecimal("300000").compareTo(mail.totalAmount()));
        assertEquals(OffsetDateTime.parse("2026-11-04T12:00:00+07:00"), mail.cancelledAt());
    }
}
