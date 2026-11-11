package com.example.order_service;

import com.example.order_service.adapter.in.scheduler.AutoCompleteOrderScheduler;
import com.example.order_service.application.dto.event.OrderStatusChangedPayload;
import com.example.order_service.application.dto.outbox.OutboxEventDto;
import com.example.order_service.application.port.in.AutoCompleteOrderUseCase;
import com.example.order_service.application.port.out.outbox.OutboxPort;
import com.example.order_service.application.port.out.repo.CheckoutRepoPort;
import com.example.order_service.application.port.out.repo.OrderRepoPort;
import com.example.order_service.application.service.AutoCompleteOrderService;
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
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// Kiểm tra job tự chuyển đơn đang giao sang hoàn thành sau 1 ngày kể từ lúc giao cho đơn vị vận chuyển.
class AutoCompleteOrderTests {
    private final Clock clock = Clock.fixed(Instant.parse("2026-11-04T05:00:00Z"), ZoneId.of("Asia/Ho_Chi_Minh"));
    private final LocalDateTime now = LocalDateTime.now(clock);
    private final OrderRepoPort orders = mock(OrderRepoPort.class);
    private final CheckoutRepoPort checkouts = mock(CheckoutRepoPort.class);
    private final OutboxPort outbox = mock(OutboxPort.class);
    private final AutoCompleteOrderService service = new AutoCompleteOrderService(orders, checkouts, outbox, clock, Duration.ofDays(1));

    // Snapshot người mua mà job đọc để dựng event gửi mail.
    {
        when(checkouts.findById(7L)).thenReturn(Optional.of(Checkout.reconstitute(7L, 1L, "Nguyễn Văn A", "buyer@example.com",
                UUID.randomUUID().toString(), "1 Lê Lợi", "127.0.0.1", null, new BigDecimal("150000"), CheckoutStatus.DONE,
                null, now.minusDays(5), now.minusDays(5))));
    }

    // Đơn đang giao với mốc giao cho ĐVVC tùy chỉnh; updatedAt bằng shippedAt hoặc mốc truyền vào khi đơn cũ chưa có shippedAt.
    private Order shippingOrder(LocalDateTime shippedAt, LocalDateTime updatedAt) {
        LocalDateTime createdAt = now.minusDays(5);
        BigDecimal price = new BigDecimal("150000");
        OrderItem item = OrderItem.reconstitute(11L, 21L, 31L, "Tai nghe", "[]", null, 1, price, price, price,
                PriceType.NORMAL, createdAt);
        return Order.reconstitute(5L, 7L, 9L, "Shop A", null, PriceType.NORMAL, CheckoutPaymentMethod.COD,
                OrderStatus.SHIPPING, price, null, null, null, createdAt.plusHours(1), shippedAt, null, createdAt,
                updatedAt, List.of(item));
    }

    @Test
    void scansShippingOrdersShippedMoreThanOneDayAgoInBatches() {
        when(orders.findShippingOrderIdsDueBefore(now.minusDays(1), 100)).thenReturn(List.of(5L, 6L));
        assertEquals(List.of(5L, 6L), service.findDueOrderIds());
    }

    @Test
    void completesDueOrderAndRecordsDeliveredAt() {
        Order order = shippingOrder(now.minusDays(1).minusMinutes(1), now.minusDays(1).minusMinutes(1));
        when(orders.findByIdForUpdate(5L)).thenReturn(Optional.of(order));

        service.complete(5L);

        assertEquals(OrderStatus.DONE, order.getStatus());
        assertEquals(now, order.getDeliveredAt());
        verify(orders).saveStatus(List.of(order));
        ArgumentCaptor<OutboxEventDto> saved = ArgumentCaptor.forClass(OutboxEventDto.class);
        verify(outbox).save(saved.capture());
        OrderStatusChangedPayload event = (OrderStatusChangedPayload) saved.getValue().payload();
        assertEquals(OrderStatusChangedPayload.EVENT_TYPE, saved.getValue().eventType());
        assertEquals(OrderStatus.DONE, event.status());
        assertEquals("buyer@example.com", event.buyerEmail());
        assertEquals(OffsetDateTime.parse("2026-11-04T12:00:00+07:00"), event.changedAt());
    }

    @Test
    void orderShippedLessThanOneDayAgoIsNotCompleted() {
        Order order = shippingOrder(now.minusHours(23), now.minusHours(23));
        when(orders.findByIdForUpdate(5L)).thenReturn(Optional.of(order));
        service.complete(5L);
        assertEquals(OrderStatus.SHIPPING, order.getStatus());
        verify(orders, never()).saveStatus(any());
        verifyNoInteractions(outbox);
    }

    @Test
    void legacyOrderWithoutShippedAtUsesUpdatedAt() {
        Order order = shippingOrder(null, now.minusDays(2));
        when(orders.findByIdForUpdate(5L)).thenReturn(Optional.of(order));
        service.complete(5L);
        assertEquals(OrderStatus.DONE, order.getStatus());
    }

    @Test
    void orderNoLongerShippingOrMissingIsSkipped() {
        LocalDateTime createdAt = now.minusDays(5);
        BigDecimal price = new BigDecimal("150000");
        OrderItem item = OrderItem.reconstitute(11L, 21L, 31L, "Tai nghe", "[]", null, 1, price, price, price,
                PriceType.NORMAL, createdAt);
        Order cancelled = Order.reconstitute(5L, 7L, 9L, "Shop A", null, PriceType.NORMAL, CheckoutPaymentMethod.COD,
                OrderStatus.CANCEL, price, OrderCancelledBy.SHOP, OrderCancelReason.OUT_OF_STOCK, createdAt, null, null,
                null, createdAt, createdAt, List.of(item));
        when(orders.findByIdForUpdate(5L)).thenReturn(Optional.of(cancelled));
        when(orders.findByIdForUpdate(6L)).thenReturn(Optional.empty());

        service.complete(5L);
        service.complete(6L);

        assertEquals(OrderStatus.CANCEL, cancelled.getStatus());
        verify(orders, never()).saveStatus(any());
        verifyNoInteractions(outbox);
    }

    @Test
    void oneFailingOrderDoesNotStopTheBatch() {
        AutoCompleteOrderUseCase useCase = mock(AutoCompleteOrderUseCase.class);
        when(useCase.findDueOrderIds()).thenReturn(List.of(5L, 6L));
        doThrow(new IllegalStateException("DB lỗi")).when(useCase).complete(5L);

        new AutoCompleteOrderScheduler(useCase).completeShippedOrders();

        verify(useCase).complete(6L);
    }
}
