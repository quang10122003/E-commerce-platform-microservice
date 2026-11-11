package com.example.order_service;

import com.example.common.exception.BusinessException;
import com.example.order_service.application.dto.event.OrderStatusChangedPayload;
import com.example.order_service.application.dto.order.UserOrderView;
import com.example.order_service.application.dto.outbox.OutboxEventDto;
import com.example.order_service.application.dto.response.UserInternalInfoResponse;
import com.example.order_service.application.dto.response.UserOrderResponse;
import com.example.order_service.application.error.OrderError;
import com.example.order_service.application.mapper.UserOrderResponseMapper;
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
import com.example.order_service.domain.model.PaymentStatus;
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

// Kiểm tra shop chuyển trạng thái đơn: bước do BE quyết định, chặn nhảy cóc/màn hình cũ, ghi mốc thời gian và event mail.
class ShopOrderAdvanceTests {
    private final Clock clock = Clock.fixed(Instant.parse("2026-11-04T05:00:00Z"), ZoneId.of("Asia/Ho_Chi_Minh"));
    private final LocalDateTime now = LocalDateTime.now(clock);
    private final LocalDateTime createdAt = now.minusHours(2);
    private final CurrentUserPort currentUser = mock(CurrentUserPort.class);
    private final OrderRepoPort orders = mock(OrderRepoPort.class);
    private final CheckoutRepoPort checkouts = mock(CheckoutRepoPort.class);
    private final ShopAvailabilityService shops = mock(ShopAvailabilityService.class);
    private final OutboxPort outbox = mock(OutboxPort.class);
    private final UserOrderService service = new UserOrderService(currentUser, orders, checkouts, shops, outbox, clock);

    private Order order(OrderStatus status, CheckoutPaymentMethod method) {
        BigDecimal price = new BigDecimal("150000");
        OrderItem item = OrderItem.reconstitute(11L, 21L, 31L, "Tai nghe", "[]", null, 1, price, price, price,
                PriceType.NORMAL, createdAt);
        boolean cancelled = status == OrderStatus.CANCEL;
        return Order.reconstitute(5L, 7L, 9L, "Shop A", null, PriceType.NORMAL, method, status, price,
                cancelled ? OrderCancelledBy.USER : null, cancelled ? OrderCancelReason.CHANGE_MIND : null,
                cancelled ? createdAt : null, null, null, null, createdAt, createdAt, List.of(item));
    }

    private void assertError(Object expected, Executable action) {
        assertEquals(expected, assertThrows(BusinessException.class, action).getErrorCode());
    }

    private void shopLoggedIn() {
        when(currentUser.getInfo()).thenReturn(UserInternalInfoResponse.builder().userId(9L).build());
        when(shops.getShopLocks(List.of(9L))).thenReturn(Map.of(9L, false));
        when(checkouts.findById(7L)).thenReturn(Optional.of(Checkout.reconstitute(7L, 1L, "Nguyễn Văn A",
                "buyer@example.com", UUID.randomUUID().toString(), "1 Lê Lợi", "127.0.0.1", null,
                new BigDecimal("150000"), CheckoutStatus.DONE, null, createdAt, createdAt)));
    }

    @Test
    void shopConfirmsCodOrderAndConfirmedAtIsRecorded() {
        Order order = order(OrderStatus.PENDING_CONFIRMATION, CheckoutPaymentMethod.COD);
        order.advanceByShop(OrderStatus.PENDING_CONFIRMATION, now);
        assertEquals(OrderStatus.PENDING_PICKUP, order.getStatus());
        assertEquals(now, order.getConfirmedAt());
        assertNull(order.getShippedAt());
    }

    @Test
    void shopHandsOverToCarrierForCodAndPaidVnpayOrders() {
        for (CheckoutPaymentMethod method : CheckoutPaymentMethod.values()) {
            Order order = order(OrderStatus.PENDING_PICKUP, method);
            order.advanceByShop(OrderStatus.PENDING_PICKUP, now);
            assertEquals(OrderStatus.SHIPPING, order.getStatus());
            assertEquals(now, order.getShippedAt());
        }
    }

    @Test
    void vnpayOrderIsConfirmedOnlyByPaymentAndStillGetsConfirmedAt() {
        Order manual = order(OrderStatus.PENDING_CONFIRMATION, CheckoutPaymentMethod.VNPAY);
        assertError(OrderDomainError.AUTO_CONFIRMED_BY_PAYMENT, () -> manual.advanceByShop(OrderStatus.PENDING_CONFIRMATION, now));
        assertEquals(OrderStatus.PENDING_CONFIRMATION, manual.getStatus());

        // Luồng IPN/đối soát gọi confirm() khi thanh toán xong.
        Order paid = order(OrderStatus.PENDING_CONFIRMATION, CheckoutPaymentMethod.VNPAY);
        paid.confirm(now);
        assertEquals(now, paid.getConfirmedAt());
    }

    @Test
    void staleCurrentStatusCannotSkipSteps() {
        // UI cũ thấy PENDING_CONFIRMATION nhưng đơn đã sang PENDING_PICKUP: không được đẩy tiếp sang SHIPPING.
        Order order = order(OrderStatus.PENDING_PICKUP, CheckoutPaymentMethod.COD);
        assertError(OrderDomainError.STATUS_CHANGED, () -> order.advanceByShop(OrderStatus.PENDING_CONFIRMATION, now));
        assertEquals(OrderStatus.PENDING_PICKUP, order.getStatus());
        assertNull(order.getShippedAt());
    }

    @Test
    void noShopStepAfterShippingOrForClosedOrders() {
        for (OrderStatus status : List.of(OrderStatus.SHIPPING, OrderStatus.DONE, OrderStatus.CANCEL)) {
            Order order = order(status, CheckoutPaymentMethod.COD);
            assertError(OrderDomainError.INVALID_STATE, () -> order.advanceByShop(status, now));
            assertEquals(status, order.getStatus());
        }
        assertError(OrderDomainError.INVALID_DATA,
                () -> order(OrderStatus.PENDING_PICKUP, CheckoutPaymentMethod.COD).advanceByShop(null, now));
    }

    @Test
    void lockedShopCannotAdvance() {
        when(currentUser.getInfo()).thenReturn(UserInternalInfoResponse.builder().userId(9L).build());
        when(shops.getShopLocks(List.of(9L))).thenReturn(Map.of(9L, true));
        assertError(OrderError.SHOP_LOCKED, () -> service.advanceShopOrder(5L, OrderStatus.PENDING_CONFIRMATION));
        verifyNoInteractions(orders, outbox);
    }

    @Test
    void advanceSavesOrderAndPublishesBuyerMailEvent() {
        shopLoggedIn();
        Order order = order(OrderStatus.PENDING_PICKUP, CheckoutPaymentMethod.COD);
        when(orders.findShopOrderForUpdate(5L, 9L)).thenReturn(Optional.of(order));

        service.advanceShopOrder(5L, OrderStatus.PENDING_PICKUP);

        verify(orders).saveStatus(List.of(order));
        ArgumentCaptor<OutboxEventDto> saved = ArgumentCaptor.forClass(OutboxEventDto.class);
        verify(outbox).save(saved.capture());
        OrderStatusChangedPayload event = (OrderStatusChangedPayload) saved.getValue().payload();
        assertEquals(OrderStatusChangedPayload.EVENT_TYPE, saved.getValue().eventType());
        assertEquals(OrderStatus.SHIPPING, event.status());
        assertEquals("buyer@example.com", event.buyerEmail());
        assertEquals("Nguyễn Văn A", event.buyerFullName());
        assertEquals("Shop A", event.shopName());
        assertEquals(OffsetDateTime.parse("2026-11-04T12:00:00+07:00"), event.changedAt());
    }

    @Test
    void rejectedAdvanceWritesNothing() {
        shopLoggedIn();
        when(orders.findShopOrderForUpdate(5L, 9L))
                .thenReturn(Optional.of(order(OrderStatus.PENDING_PICKUP, CheckoutPaymentMethod.COD)));
        assertError(OrderDomainError.STATUS_CHANGED, () -> service.advanceShopOrder(5L, OrderStatus.PENDING_CONFIRMATION));
        verify(orders, never()).saveStatus(any());
        verifyNoInteractions(outbox);
    }

    @Test
    void responseExposesTimelineWithOffsets() {
        UserOrderView view = new UserOrderView(5L, 7L, OrderStatus.SHIPPING, 9L, "Shop A", null, PriceType.NORMAL,
                CheckoutPaymentMethod.VNPAY, new BigDecimal("150000"), null, null, null, now.minusMinutes(30),
                now.minusMinutes(10), null, createdAt, PaymentStatus.DONE, null, now.minusMinutes(50), now.minusMinutes(60));

        UserOrderResponse response = UserOrderResponseMapper.toResponse(view, List.of(), now, clock.getZone());

        assertEquals(OffsetDateTime.parse("2026-11-04T11:30:00+07:00"), response.confirmedAt());
        assertEquals(OffsetDateTime.parse("2026-11-04T11:50:00+07:00"), response.shippedAt());
        assertEquals(OffsetDateTime.parse("2026-11-04T11:00:00+07:00"), response.payment().paidAt());
    }
}
