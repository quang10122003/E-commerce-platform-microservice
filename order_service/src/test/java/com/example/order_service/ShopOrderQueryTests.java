package com.example.order_service;

import com.example.common.exception.BusinessException;
import com.example.order_service.adapter.mapper.OrderMapper;
import com.example.order_service.adapter.out.persistence.CheckoutJpaRepo;
import com.example.order_service.adapter.out.persistence.OrderItemJpaRepo;
import com.example.order_service.adapter.out.persistence.OrderJpaRepo;
import com.example.order_service.adapter.out.persistence.OrderRepoAdapter;
import com.example.order_service.application.dto.order.ShopOrderSearch;
import com.example.order_service.application.dto.order.ShopOrderView;
import com.example.order_service.application.dto.order.UserOrderItemView;
import com.example.order_service.application.dto.order.UserOrderView;
import com.example.order_service.application.dto.request.ShopOrderFilter;
import com.example.order_service.application.dto.response.ShopOrderResponse;
import com.example.order_service.application.dto.response.UserInternalInfoResponse;
import com.example.order_service.application.dto.response.UserOrderResponse;
import com.example.order_service.application.error.OrderError;
import com.example.order_service.application.port.out.client.CurrentUserPort;
import com.example.order_service.application.port.out.outbox.OutboxPort;
import com.example.order_service.application.port.out.repo.CheckoutRepoPort;
import com.example.order_service.application.port.out.repo.OrderRepoPort;
import com.example.order_service.application.service.ShopAvailabilityService;
import com.example.order_service.application.service.UserOrderService;
import com.example.order_service.domain.model.CheckoutPaymentMethod;
import com.example.order_service.domain.model.OrderStatus;
import com.example.order_service.domain.model.PaymentStatus;
import com.example.order_service.domain.model.PriceType;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

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

// Kiểm tra API đơn của shop: lọc theo shop hiện tại, chuẩn hóa bộ lọc, kèm người mua và không lộ link thanh toán.
class ShopOrderQueryTests {
    private final Clock clock = Clock.fixed(Instant.parse("2026-11-04T05:00:00Z"), ZoneId.of("Asia/Ho_Chi_Minh"));
    private final LocalDateTime now = LocalDateTime.now(clock);
    private final CurrentUserPort currentUser = mock(CurrentUserPort.class);
    private final OrderRepoPort orders = mock(OrderRepoPort.class);
    private final UserOrderService service = new UserOrderService(currentUser, orders, mock(CheckoutRepoPort.class), mock(ShopAvailabilityService.class), mock(OutboxPort.class), clock);
    private final ShopOrderResponse.Buyer buyer = new ShopOrderResponse.Buyer("Nguyễn Văn A", "a@example.com",
            "1 Lê Lợi, Quận 1", "Giao giờ hành chính");
    private final ShopOrderFilter noFilter = new ShopOrderFilter(null, null, null, null, null);

    // Đơn VNPay còn hạn thanh toán: user thấy link, shop thì không.
    private UserOrderView payableOrder() {
        return new UserOrderView(5L, 7L, OrderStatus.PENDING_CONFIRMATION, 9L, "Shop A", null, PriceType.NORMAL,
                CheckoutPaymentMethod.VNPAY, new BigDecimal("200000"), null, null, null, null, null, null, now.minusMinutes(1),
                PaymentStatus.PENDING, "https://sandbox.vnpayment.vn/pay", now.plusMinutes(10), null);
    }

    private UserOrderItemView item() {
        return new UserOrderItemView(5L, new UserOrderResponse.Item(11L, 21L, 31L, "Tai nghe", "[]", null, 1,
                new BigDecimal("200000"), new BigDecimal("200000"), new BigDecimal("200000")));
    }

    private void loggedInShop() {
        when(currentUser.getInfo()).thenReturn(UserInternalInfoResponse.builder().userId(9L).build());
    }

    private ShopOrderSearch searchFor(ShopOrderFilter filter) {
        when(orders.findShopOrders(any())).thenReturn(List.of());
        service.getShopOrders(filter);
        ArgumentCaptor<ShopOrderSearch> search = ArgumentCaptor.forClass(ShopOrderSearch.class);
        verify(orders).findShopOrders(search.capture());
        return search.getValue();
    }

    @Test
    void shopSeesOwnOrdersWithBuyerSnapshotButNoPaymentUrl() {
        loggedInShop();
        when(orders.findShopOrders(any())).thenReturn(List.of(new ShopOrderView(payableOrder(), buyer)));
        when(orders.findUserOrderItems(List.of(5L))).thenReturn(List.of(item()));

        ShopOrderResponse response = service.getShopOrders(noFilter).get(0);

        assertEquals(buyer, response.buyer());
        assertEquals(1, response.items().size());
        assertEquals(PaymentStatus.PENDING, response.payment().status());
        assertNull(response.payment().paymentUrl());
        assertNotNull(response.payment().expiresAt());
    }

    @Test
    void filtersAreNormalizedAndTimesConvertedToDatabaseZone() {
        loggedInShop();
        ShopOrderSearch search = searchFor(new ShopOrderFilter(OrderStatus.DONE, CheckoutPaymentMethod.COD, "  #54 ",
                OffsetDateTime.parse("2026-11-01T00:00:00Z"), OffsetDateTime.parse("2026-11-03T23:59:00+07:00")));

        assertEquals(9L, search.shopId());
        assertEquals(OrderStatus.DONE, search.status());
        assertEquals(CheckoutPaymentMethod.COD, search.paymentMethod());
        assertEquals("54", search.keyword());
        assertEquals(54L, search.orderId());
        // 00:00 UTC là 07:00 giờ Việt Nam; mốc đã ở +07:00 thì giữ nguyên giờ.
        assertEquals(LocalDateTime.parse("2026-11-01T07:00:00"), search.from());
        assertEquals(LocalDateTime.parse("2026-11-03T23:59:00"), search.to());
    }

    @Test
    void textKeywordDoesNotMatchOrderIdAndBlankKeywordIsIgnored() {
        loggedInShop();
        ShopOrderSearch text = searchFor(new ShopOrderFilter(null, null, "Tai nghe", null, null));
        assertEquals("Tai nghe", text.keyword());
        assertNull(text.orderId());

        reset(orders);
        ShopOrderSearch blank = searchFor(new ShopOrderFilter(null, null, "   ", null, null));
        assertNull(blank.keyword());
        assertNull(blank.orderId());
    }

    @Test
    void reversedDateRangeIsRejectedBeforeQuery() {
        BusinessException error = assertThrows(BusinessException.class, () -> service.getShopOrders(new ShopOrderFilter(
                null, null, null, OffsetDateTime.parse("2026-11-03T00:00:00+07:00"),
                OffsetDateTime.parse("2026-11-01T00:00:00+07:00"))));
        assertEquals(OrderError.INVALID_DATE_RANGE, error.getErrorCode());
        verifyNoInteractions(orders);
    }

    @Test
    void adapterEscapesLikeWildcardsInKeyword() {
        OrderJpaRepo jpa = mock(OrderJpaRepo.class);
        OrderRepoAdapter adapter = new OrderRepoAdapter(jpa, mock(OrderItemJpaRepo.class), mock(CheckoutJpaRepo.class),
                mock(OrderMapper.class));
        adapter.findShopOrders(new ShopOrderSearch(9L, null, null, "50%_off!", null, null, null));
        verify(jpa).findShopOrders(9L, null, null, "50!%!_off!!", null, null, null);
    }

    @Test
    void userStillGetsPaymentUrlWhilePayable() {
        when(currentUser.getInfo()).thenReturn(UserInternalInfoResponse.builder().userId(1L).build());
        when(orders.findUserOrders(1L, null)).thenReturn(List.of(payableOrder()));
        when(orders.findUserOrderItems(List.of(5L))).thenReturn(List.of(item()));

        assertEquals("https://sandbox.vnpayment.vn/pay", service.getMyOrders(null).get(0).payment().paymentUrl());
    }

    @Test
    void shopWithoutOrdersSkipsItemQuery() {
        loggedInShop();
        when(orders.findShopOrders(any())).thenReturn(List.of());
        assertTrue(service.getShopOrders(noFilter).isEmpty());
        verify(orders, never()).findUserOrderItems(anyCollection());
    }

    @Test
    void jsonKeepsUserOrderFieldsFlatAndAddsBuyer() {
        loggedInShop();
        when(orders.findShopOrders(any())).thenReturn(List.of(new ShopOrderView(payableOrder(), buyer)));
        when(orders.findUserOrderItems(List.of(5L))).thenReturn(List.of(item()));

        JsonNode json = JsonMapper.builder().build().valueToTree(service.getShopOrders(noFilter).get(0));

        assertFalse(json.has("order"));
        assertEquals(5L, json.get("orderId").asLong());
        assertTrue(json.get("items").isArray());
        assertEquals("Nguyễn Văn A", json.get("buyer").get("fullName").asString());
    }
}
