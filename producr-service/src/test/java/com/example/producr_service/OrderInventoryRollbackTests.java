package com.example.producr_service;

import com.example.producr_service.application.dto.checkout.CheckoutItemSnapshotDto;
import com.example.producr_service.application.dto.checkout.CheckoutSaleDto;
import com.example.producr_service.application.dto.checkout.CheckoutVariantDto;
import com.example.producr_service.application.dto.outbox.OutboxEventDto;
import com.example.producr_service.application.dto.outbox.ProductUpdatedOutboxPayload;
import com.example.producr_service.application.dto.saga.CheckoutInventoryRollbackRequestedPayload;
import com.example.producr_service.application.dto.saga.CheckoutInventoryRolledBackPayload;
import com.example.producr_service.application.dto.saga.CheckoutInventorySucceededPayload;
import com.example.producr_service.application.dto.saga.OrderInventoryRollbackRequestedPayload;
import com.example.producr_service.application.port.out.Json.MapJsonToObjPort;
import com.example.producr_service.application.port.out.outbox.OutboxPort;
import com.example.producr_service.application.port.out.repo.FlashSaleCampaignRepositoryPort;
import com.example.producr_service.application.port.out.repo.FlashSaleItemRepositoryPort;
import com.example.producr_service.application.port.out.repo.InboxEventPort;
import com.example.producr_service.application.port.out.repo.ProductRepositoryPort;
import com.example.producr_service.application.port.out.repo.ProductVariantRepoPort;
import com.example.producr_service.application.registry.KafkaEventHandlerRegistry;
import com.example.producr_service.application.service.CheckoutInventoryService;
import com.example.producr_service.application.service.OrderEventApplicationService;
import com.example.producr_service.application.strategy.eventKafka.OrderInventoryRollbackRequestedHandler;
import com.example.producr_service.domain.model.FlashSaleCampaignStatus;
import com.example.producr_service.domain.model.FlashSaleItemStatus;
import com.example.producr_service.domain.model.InboxEvent;
import com.example.producr_service.domain.model.Money;
import com.example.producr_service.domain.model.ProductStatus;
import com.example.producr_service.domain.model.ProductVariant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;
import org.springframework.transaction.support.SimpleTransactionStatus;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

// Kiểm tra saga hoàn tồn kho theo từng order khi user hủy, và rollback cả checkout vẫn như cũ sau khi tách hàm.
class OrderInventoryRollbackTests {
    private final Clock clock = Clock.fixed(Instant.parse("2026-11-04T05:00:00Z"), ZoneId.of("Asia/Ho_Chi_Minh"));
    private final LocalDateTime now = LocalDateTime.now(clock);
    private final InboxEventPort inbox = mock(InboxEventPort.class);
    private final ProductRepositoryPort products = mock(ProductRepositoryPort.class);
    private final ProductVariantRepoPort variants = mock(ProductVariantRepoPort.class);
    private final FlashSaleCampaignRepositoryPort campaigns = mock(FlashSaleCampaignRepositoryPort.class);
    private final FlashSaleItemRepositoryPort saleItems = mock(FlashSaleItemRepositoryPort.class);
    private final OutboxPort outbox = mock(OutboxPort.class);
    private final MapJsonToObjPort json = mock(MapJsonToObjPort.class);
    private final CheckoutInventoryService service = new CheckoutInventoryService(
            inbox, products, variants, campaigns, saleItems, outbox, json, clock, null, null);
    private final UUID inventorySucceededId = UUID.randomUUID();
    private final UUID checkoutSucceededId = UUID.randomUUID();
    // Inbox đã có các event id này; mặc định CHECKOUT_SUCCEEDED đã được Product xử lý.
    private final Set<UUID> processed = new HashSet<>();

    // Checkout 7 gồm 3 order: shop 9 giá thường (A), shop 9 Flash Sale (B), shop 10 giá thường (C).
    private final CheckoutItemSnapshotDto itemA = item(201L, 101L, 9L, 2, false, null);
    private final CheckoutItemSnapshotDto itemB = item(202L, 102L, 9L, 1, true, 301L);
    private final CheckoutItemSnapshotDto itemC = item(203L, 103L, 10L, 4, false, null);

    private static CheckoutItemSnapshotDto item(long productId, long variantId, long shopId, int quantity,
                                                boolean sale, Long saleItemId) {
        return new CheckoutItemSnapshotDto(productId, variantId, shopId, "Sản phẩm", "[]", null, quantity,
                BigDecimal.valueOf(1000), BigDecimal.valueOf(sale ? 800 : 1000), sale, saleItemId);
    }

    @BeforeEach
    void prepare() {
        processed.add(checkoutSucceededId);
        when(inbox.tryInsert(any())).thenAnswer(call -> processed.add(((InboxEvent) call.getArgument(0)).getEventId()));
        when(json.writeValue(any())).thenReturn("{}");
        CheckoutInventorySucceededPayload snapshot = new CheckoutInventorySucceededPayload(inventorySucceededId,
                CheckoutInventorySucceededPayload.EVENT_TYPE, 7L, UUID.randomUUID(), List.of(itemA, itemB, itemC),
                BigDecimal.valueOf(6800), OffsetDateTime.now(clock));
        when(outbox.findByEventId(inventorySucceededId)).thenReturn(Optional.of(new OutboxEventDto(inventorySucceededId,
                "CHECKOUT", "7", snapshot.eventType(), snapshot, now)));
        when(json.readValue((Object) snapshot, CheckoutInventorySucceededPayload.class)).thenReturn(snapshot);
        // Mỗi phân loại còn 10 trong kho; product id = variant id + 100.
        when(variants.lockVariantsForUpdate(anyCollection())).thenAnswer(call -> ((Collection<Long>) call.getArgument(0))
                .stream().map((Long id) -> new CheckoutVariantDto(id + 100, 9L, "Sản phẩm", ProductStatus.ACTIVE, "[]",
                        null, ProductVariant.reconstitute(id, "SKU-" + id, Money.of(1000), 10))).toList());
        when(variants.findProductIdsByVariantIds(anyCollection())).thenAnswer(call -> ((Collection<Long>) call.getArgument(0))
                .stream().map((Long id) -> id + 100).toList());
        when(saleItems.findCampaignIdsByItemIds(anyList())).thenAnswer(call -> ((List<Long>) call.getArgument(0))
                .isEmpty() ? List.of() : List.of(401L));
        when(saleItems.lockItemsForUpdate(anyList())).thenAnswer(call -> ((List<Long>) call.getArgument(0)).stream()
                .map((Long id) -> new CheckoutSaleDto(id, 401L, 102L, BigDecimal.TEN, 10, 5,
                        FlashSaleItemStatus.REGISTERED, FlashSaleCampaignStatus.PUBLISHED, now.minusHours(1), now.plusHours(1)))
                .toList());
        when(products.decreaseTotalSold(anyLong(), anyLong())).thenReturn(1);
    }

    private OrderInventoryRollbackRequestedPayload orderEvent(Long shopId, boolean sale) {
        return new OrderInventoryRollbackRequestedPayload(UUID.randomUUID(),
                OrderInventoryRollbackRequestedPayload.EVENT_TYPE, 7L, 5L, shopId, sale, inventorySucceededId,
                checkoutSucceededId, OffsetDateTime.now(clock));
    }

    private List<OutboxEventDto> savedOutbox() {
        ArgumentCaptor<OutboxEventDto> saved = ArgumentCaptor.forClass(OutboxEventDto.class);
        verify(outbox, atLeast(0)).save(saved.capture());
        return saved.getAllValues();
    }

    @Test
    void saleOrderRestoresOnlyItsItemsThroughKafkaRouter() {
        OrderInventoryRollbackRequestedPayload event = orderEvent(9L, true);
        when(json.readValue("message", OrderInventoryRollbackRequestedPayload.class)).thenReturn(event);
        new OrderEventApplicationService(new KafkaEventHandlerRegistry(
                List.of(new OrderInventoryRollbackRequestedHandler(json, service))))
                .process(event.eventId().toString(), event.eventType(), "message");

        verify(variants).updateStock(102L, 11);
        verify(variants, never()).updateStock(eq(101L), anyInt());
        verify(variants, never()).updateStock(eq(103L), anyInt());
        verify(saleItems).updateSoldQuantity(301L, 4L, now);
        verify(products).decreaseTotalSold(202L, 1L);
        verify(products, never()).decreaseTotalSold(eq(201L), anyLong());
        verify(products, never()).decreaseTotalSold(eq(203L), anyLong());
        // Chỉ báo Product thay đổi; không gửi kết quả rollback checkout về Order.
        List<OutboxEventDto> saved = savedOutbox();
        assertEquals(1, saved.size());
        assertEquals(ProductUpdatedOutboxPayload.EVENT_TYPE, saved.get(0).eventType());
        assertEquals("202", saved.get(0).aggregateId());
    }

    @Test
    void normalOrderOfSameShopDoesNotTouchSaleQuota() {
        service.rollbackOrderStock(orderEvent(9L, false));
        verify(variants).updateStock(101L, 12);
        verify(variants, never()).updateStock(eq(102L), anyInt());
        verify(saleItems, never()).updateSoldQuantity(anyLong(), anyLong(), any());
        verify(products).decreaseTotalSold(201L, 2L);
    }

    @Test
    void replayedEventIsIgnored() {
        OrderInventoryRollbackRequestedPayload event = orderEvent(10L, false);
        service.rollbackOrderStock(event);
        service.rollbackOrderStock(event);
        verify(variants, times(1)).updateStock(103L, 14);
        verify(products, times(1)).decreaseTotalSold(203L, 4L);
    }

    @Test
    void waitsForCheckoutSucceededAndRollsBackMarker() {
        processed.remove(checkoutSucceededId);
        PlatformTransactionManager transactions = mock(PlatformTransactionManager.class);
        SimpleTransactionStatus status = new SimpleTransactionStatus();
        when(transactions.getTransaction(any())).thenReturn(status);
        ProxyFactory factory = new ProxyFactory(service);
        factory.addAdvice(new TransactionInterceptor(transactions, new AnnotationTransactionAttributeSource()));
        CheckoutInventoryService proxy = (CheckoutInventoryService) factory.getProxy();

        assertThrows(IllegalStateException.class, () -> proxy.rollbackOrderStock(orderEvent(9L, false)));
        verify(transactions).rollback(status);
        verify(transactions, never()).commit(any());
        verify(variants, never()).updateStock(anyLong(), anyInt());
        verify(products, never()).decreaseTotalSold(anyLong(), anyLong());
    }

    @Test
    void orderWithoutMatchingItemsFails() {
        assertThrows(IllegalArgumentException.class, () -> service.rollbackOrderStock(orderEvent(99L, false)));
        verify(variants, never()).updateStock(anyLong(), anyInt());
        verify(outbox, never()).save(any());
    }

    @Test
    void invalidMetadataFailsBeforeInbox() {
        OrderInventoryRollbackRequestedPayload missingShop = new OrderInventoryRollbackRequestedPayload(UUID.randomUUID(),
                OrderInventoryRollbackRequestedPayload.EVENT_TYPE, 7L, 5L, null, false, inventorySucceededId,
                checkoutSucceededId, OffsetDateTime.now(clock));
        assertThrows(IllegalArgumentException.class, () -> service.rollbackOrderStock(missingShop));
        verify(inbox, never()).tryInsert(any());
    }

    @Test
    void checkoutRollbackStillRestoresEveryOrderAndReportsBack() {
        CheckoutInventoryRollbackRequestedPayload event = new CheckoutInventoryRollbackRequestedPayload(UUID.randomUUID(),
                CheckoutInventoryRollbackRequestedPayload.EVENT_TYPE, 7L, inventorySucceededId, checkoutSucceededId,
                "CHECKOUT_PAYMENT_FAILED", "VNPay không ghi nhận", OffsetDateTime.now(clock));
        service.rollbackCheckoutStock(event);

        verify(variants).updateStock(101L, 12);
        verify(variants).updateStock(102L, 11);
        verify(variants).updateStock(103L, 14);
        verify(saleItems).updateSoldQuantity(301L, 4L, now);
        verify(products).decreaseTotalSold(201L, 2L);
        verify(products).decreaseTotalSold(202L, 1L);
        verify(products).decreaseTotalSold(203L, 4L);
        assertTrue(savedOutbox().stream().anyMatch(
                (OutboxEventDto value) -> CheckoutInventoryRolledBackPayload.EVENT_TYPE.equals(value.eventType())));
    }
}
