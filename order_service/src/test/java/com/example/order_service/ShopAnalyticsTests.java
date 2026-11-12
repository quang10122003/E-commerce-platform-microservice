package com.example.order_service;

import com.example.common.exception.BusinessException;
import com.example.order_service.application.dto.order.ShopAnalyticsOrderView;
import com.example.order_service.application.dto.response.AdminAnalyticsResponse;
import com.example.order_service.application.dto.response.ShopAnalyticsResponse;
import com.example.order_service.application.dto.response.UserInternalInfoResponse;
import com.example.order_service.application.port.out.client.CurrentUserPort;
import com.example.order_service.application.port.out.repo.ShopAnalyticsQueryPort;
import com.example.order_service.application.service.ShopAnalyticsService;
import com.example.order_service.domain.model.CheckoutPaymentMethod;
import com.example.order_service.domain.model.OrderCancelReason;
import com.example.order_service.domain.model.OrderCancelledBy;
import com.example.order_service.domain.model.OrderStatus;
import com.example.order_service.domain.model.PriceType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

// Kiểm tra thống kê shop: phân nhóm tiền, kỳ trước và gom ngày theo offset của người xem.
class ShopAnalyticsTests {
    private final Clock clock = Clock.fixed(Instant.parse("2026-11-12T05:00:00Z"), ZoneId.of("Asia/Ho_Chi_Minh"));
    private final CurrentUserPort currentUser = mock(CurrentUserPort.class);
    private final ShopAnalyticsQueryPort query = mock(ShopAnalyticsQueryPort.class);
    private final ShopAnalyticsService service = new ShopAnalyticsService(currentUser, query, clock);
    // Người xem ở +09:00 trong khi DB lưu giờ +07:00.
    private final OffsetDateTime from = OffsetDateTime.parse("2026-11-11T00:00:00+09:00");
    private final OffsetDateTime to = OffsetDateTime.parse("2026-11-12T23:59:59+09:00");

    private ShopAnalyticsOrderView order(OrderStatus status, CheckoutPaymentMethod method, String amount,
                                         String createdAt, String deliveredAt, String cancelledAt, String paidAt) {
        boolean cancelled = status == OrderStatus.CANCEL;
        return new ShopAnalyticsOrderView(status, method, PriceType.NORMAL, new BigDecimal(amount),
                cancelled ? OrderCancelledBy.SHOP : null, cancelled ? OrderCancelReason.OUT_OF_STOCK : null,
                LocalDateTime.parse(createdAt), null, null, deliveredAt == null ? null : LocalDateTime.parse(deliveredAt),
                cancelledAt == null ? null : LocalDateTime.parse(cancelledAt),
                paidAt == null ? null : LocalDateTime.parse(paidAt), 1L);
    }

    @Test
    void classifiesMoneyAndBucketsByViewerOffset() {
        when(currentUser.getInfo()).thenReturn(mock(UserInternalInfoResponse.class));
        when(query.sumSaleDiscount(any(), any(), any())).thenReturn(BigDecimal.ZERO);
        when(query.findOrdersTouching(any(), any(), any())).thenReturn(List.of(
                order(OrderStatus.DONE, CheckoutPaymentMethod.COD, "100", "2026-11-10T23:30:00", "2026-11-11T10:00:00", null, null),
                order(OrderStatus.PENDING_PICKUP, CheckoutPaymentMethod.VNPAY, "200", "2026-11-11T08:00:00", null, null, "2026-11-11T08:05:00"),
                order(OrderStatus.PENDING_CONFIRMATION, CheckoutPaymentMethod.VNPAY, "300", "2026-11-11T09:00:00", null, null, null),
                order(OrderStatus.CANCEL, CheckoutPaymentMethod.VNPAY, "400", "2026-11-11T09:30:00", null, "2026-11-11T12:00:00", "2026-11-11T09:35:00"),
                order(OrderStatus.PENDING_CONFIRMATION, CheckoutPaymentMethod.COD, "50", "2026-11-09T12:00:00", null, null, null)));

        ShopAnalyticsResponse response = service.getShopAnalytics(from, to);

        ShopAnalyticsResponse.Overview overview = response.overview();
        assertEquals(new ShopAnalyticsResponse.Money(4, new BigDecimal("1000")), overview.placed());
        assertEquals(new BigDecimal("100"), overview.received().amount());
        assertEquals(new BigDecimal("200"), overview.held().amount());
        assertEquals(new BigDecimal("300"), overview.awaitingPayment().amount());
        assertEquals(0, overview.expectedCod().orderCount());
        assertEquals(new BigDecimal("400"), overview.lost().amount());
        assertEquals(new BigDecimal("400"), overview.refundDue().amount());
        assertEquals(new BigDecimal("50"), response.previousOverview().expectedCod().amount());
        assertEquals(1, response.previousOverview().placed().orderCount());

        assertEquals(2, response.timeline().size());
        ShopAnalyticsResponse.DailyPoint firstDay = response.timeline().get(0);
        assertEquals(from, firstDay.bucketStart());
        assertEquals(4, firstDay.placedOrders());
        assertEquals(1, firstDay.receivedOrders());
        assertEquals(1, firstDay.cancelledOrders());
        assertEquals(new BigDecimal("600"), firstDay.paidOnlineAmount());
        // Đơn đặt 23:30 ngày 10 giờ +07:00 là 01:30 ngày 11 với người xem +09:00.
        assertEquals(1L, response.ordersByHour().get(1));
        assertEquals(3, response.byPaymentMethod().stream().mapToLong(ShopAnalyticsResponse.Slice::orderCount).sum());
        assertEquals(OrderCancelReason.OUT_OF_STOCK, response.cancellations().get(0).reason());
    }

    @Test
    void adminAnalyticsCoversEveryShop() {
        when(query.sumSaleDiscount(isNull(), any(), any())).thenReturn(BigDecimal.ZERO);
        when(query.findOrdersTouching(isNull(), any(), any())).thenReturn(List.of(
                order(OrderStatus.DONE, CheckoutPaymentMethod.COD, "100", "2026-11-11T08:00:00", "2026-11-11T10:00:00", null, null)));
        when(query.countActiveShops(any(), any())).thenReturn(3L);
        when(query.findTopShops(any(), any(), eq(10))).thenReturn(List.of(
                new AdminAnalyticsResponse.TopShop(9L, "Shop A", 1, new BigDecimal("100"))));

        AdminAnalyticsResponse response = service.getAdminAnalytics(from, to);

        assertEquals(new BigDecimal("100"), response.summary().overview().received().amount());
        assertEquals(3, response.activeShopCount());
        assertEquals("Shop A", response.topShops().get(0).shopName());
        verifyNoInteractions(currentUser);
    }

    @Test
    void rejectsRangeLongerThanOneYear() {
        assertThrows(BusinessException.class, () -> service.getShopAnalytics(from, from.plusDays(366)));
        assertThrows(BusinessException.class, () -> service.getShopAnalytics(to, from));
    }
}
