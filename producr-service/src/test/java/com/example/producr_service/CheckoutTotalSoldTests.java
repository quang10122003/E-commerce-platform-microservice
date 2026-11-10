package com.example.producr_service;

import com.example.producr_service.application.dto.checkout.CheckoutItemSnapshotDto;
import com.example.producr_service.application.dto.outbox.OutboxEventDto;
import com.example.producr_service.application.dto.outbox.ProductUpdatedOutboxPayload;
import com.example.producr_service.application.dto.saga.CheckoutInventorySucceededPayload;
import com.example.producr_service.application.dto.saga.CheckoutSucceededPayload;
import com.example.producr_service.application.port.out.Json.MapJsonToObjPort;
import com.example.producr_service.application.port.out.outbox.OutboxPort;
import com.example.producr_service.application.port.out.repo.InboxEventPort;
import com.example.producr_service.application.port.out.repo.ProductBehaviorRepositoryPort;
import com.example.producr_service.application.port.out.repo.ProductRepositoryPort;
import com.example.producr_service.application.registry.KafkaEventHandlerRegistry;
import com.example.producr_service.application.service.CheckoutInventoryService;
import com.example.producr_service.application.service.OrderEventApplicationService;
import com.example.producr_service.application.strategy.eventKafka.CheckoutSucceededHandler;
import com.example.producr_service.domain.model.BehaviorType;
import com.example.producr_service.domain.model.UserProductBehavior;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;
import org.springframework.transaction.support.SimpleTransactionStatus;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class CheckoutTotalSoldTests {
    private final Clock clock = Clock.fixed(Instant.parse("2026-11-04T05:00:00Z"), ZoneId.of("Asia/Ho_Chi_Minh"));
    private final InboxEventPort inbox = mock(InboxEventPort.class);
    private final ProductRepositoryPort products = mock(ProductRepositoryPort.class);
    private final OutboxPort outbox = mock(OutboxPort.class);
    private final MapJsonToObjPort json = mock(MapJsonToObjPort.class);
    private final ProductBehaviorRepositoryPort behaviors = mock(ProductBehaviorRepositoryPort.class);
    private final CheckoutInventoryService service = new CheckoutInventoryService(
            inbox, products, null, null, null, outbox, json, clock, null, behaviors);
    private final UUID inventoryId = UUID.randomUUID();

    private CheckoutSucceededPayload event(String method) {
        return new CheckoutSucceededPayload(UUID.randomUUID(), CheckoutSucceededPayload.EVENT_TYPE,
                1L, 2L, "DONE", method, List.of(3L), BigDecimal.TEN, OffsetDateTime.now(clock),
                null, null, null, inventoryId);
    }

    private CheckoutSucceededPayload eventAt(OffsetDateTime occurredAt) {
        return new CheckoutSucceededPayload(UUID.randomUUID(), CheckoutSucceededPayload.EVENT_TYPE,
                1L, 2L, "DONE", "COD", List.of(3L), BigDecimal.TEN, occurredAt, null, null, null, inventoryId);
    }

    private void prepare(long checkoutId) {
        when(json.writeValue(any())).thenReturn("{}");
        CheckoutInventorySucceededPayload snapshot = new CheckoutInventorySucceededPayload(inventoryId,
                CheckoutInventorySucceededPayload.EVENT_TYPE, checkoutId, UUID.randomUUID(),
                List.of(item(2), item(3)), BigDecimal.TEN, OffsetDateTime.now(clock));
        when(inbox.tryInsert(any())).thenReturn(true);
        when(outbox.findByEventId(inventoryId)).thenReturn(Optional.of(new OutboxEventDto(inventoryId,
                "CHECKOUT", "1", snapshot.eventType(), snapshot, LocalDateTime.now(clock))));
        when(json.readValue((Object) snapshot, CheckoutInventorySucceededPayload.class)).thenReturn(snapshot);
        when(products.increaseTotalSold(5L, 5L)).thenReturn(1);
    }

    private CheckoutItemSnapshotDto item(int quantity) {
        return new CheckoutItemSnapshotDto(5L, (long) quantity, 7L, "Sản phẩm", "{}", null,
                quantity, BigDecimal.ONE, BigDecimal.ONE, false, null);
    }

    @Test
    void routesBothPaymentMethodsAndPublishesOnlyProductUpdates() {
        prepare(1L);
        OrderEventApplicationService router = new OrderEventApplicationService(new KafkaEventHandlerRegistry(
                List.of(new CheckoutSucceededHandler(json, service))));
        for (String method : List.of("COD", "VNPAY")) {
            CheckoutSucceededPayload event = event(method);
            when(json.readValue(method, CheckoutSucceededPayload.class)).thenReturn(event);
            router.process(event.eventId().toString(), event.eventType(), method);
        }
        verify(products, times(2)).increaseTotalSold(5L, 5L);
        verify(behaviors, times(2)).saveIfAbsent(any());
        ArgumentCaptor<OutboxEventDto> published = ArgumentCaptor.forClass(OutboxEventDto.class);
        verify(outbox, times(2)).save(published.capture());
        assertTrue(published.getAllValues().stream().allMatch(
                (OutboxEventDto value) -> ProductUpdatedOutboxPayload.EVENT_TYPE.equals(value.eventType())));
    }

    @Test
    void replayDoesNotIncreaseAgain() {
        prepare(1L);
        when(inbox.tryInsert(any())).thenReturn(true, false);
        CheckoutSucceededPayload event = event("COD");
        service.increaseCheckoutTotalSold(event);
        service.increaseCheckoutTotalSold(event);
        verify(products).increaseTotalSold(5L, 5L);
        verify(outbox).save(any());
    }

    @Test
    void mismatchedSnapshotFailsWithoutPublishing() {
        prepare(99L);
        assertThrows(IllegalArgumentException.class, () -> service.increaseCheckoutTotalSold(event("COD")));
        verify(outbox).findByEventId(inventoryId);
        verifyNoInteractions(products);
        verify(outbox, never()).save(any());
    }

    @Test
    void missingProductFailsWithoutCompensation() {
        prepare(1L);
        when(products.increaseTotalSold(5L, 5L)).thenReturn(0);
        assertThrows(IllegalStateException.class, () -> service.increaseCheckoutTotalSold(event("VNPAY")));
        verify(outbox, never()).save(any());
    }

    @Test
    void outboxFailureRollsBackAndPropagatesThroughHandlerForKafka() {
        prepare(1L);
        IllegalStateException failure = new IllegalStateException("DB unavailable");
        doThrow(failure).when(outbox).save(any());
        PlatformTransactionManager transactions = mock(PlatformTransactionManager.class);
        SimpleTransactionStatus status = new SimpleTransactionStatus();
        when(transactions.getTransaction(any())).thenReturn(status);
        ProxyFactory factory = new ProxyFactory(service);
        factory.addAdvice(new TransactionInterceptor(transactions, new AnnotationTransactionAttributeSource()));
        CheckoutInventoryService proxy = (CheckoutInventoryService) factory.getProxy();
        CheckoutSucceededPayload event = event("VNPAY");
        when(json.readValue("message", CheckoutSucceededPayload.class)).thenReturn(event);
        CheckoutSucceededHandler handler = new CheckoutSucceededHandler(json, proxy);
        assertSame(failure, assertThrows(IllegalStateException.class, () -> handler.handle("message")));
        verify(transactions).rollback(status);
        verify(transactions, never()).commit(any());
        verify(outbox).save(any());
    }

    @Test
    void processCheckoutSucceededIncreasesSoldAndRecordsPurchaseBehavior() {
        prepare(1L);
        CheckoutSucceededPayload event = event("COD");
        service.processCheckoutSucceeded(event);
        verify(products).increaseTotalSold(5L, 5L);
        ArgumentCaptor<UserProductBehavior> saved = ArgumentCaptor.forClass(UserProductBehavior.class);
        verify(behaviors).saveIfAbsent(saved.capture());
        UserProductBehavior behavior = saved.getValue();
        assertEquals(2L, behavior.getUserId());
        assertEquals(5L, behavior.getProductId());
        assertEquals(BehaviorType.PURCHASE, behavior.getEventType());
        assertEquals(event.occurredAt().toInstant(), behavior.getCreatedAt());
        assertEquals(UUID.nameUUIDFromBytes((event.eventId() + ":5").getBytes(StandardCharsets.UTF_8)).toString(),
                behavior.getEventId());
    }

    @Test
    void increaseCheckoutTotalSoldAloneDoesNotRecordBehavior() {
        prepare(1L);
        service.increaseCheckoutTotalSold(event("COD"));
        verifyNoInteractions(behaviors);
    }

    @Test
    void replayKeepsSameBehaviorEventIdSoDatabaseDeduplicates() {
        prepare(1L);
        when(inbox.tryInsert(any())).thenReturn(true, false);
        CheckoutSucceededPayload event = event("COD");
        service.processCheckoutSucceeded(event);
        service.processCheckoutSucceeded(event);
        verify(products).increaseTotalSold(5L, 5L);
        ArgumentCaptor<UserProductBehavior> saved = ArgumentCaptor.forClass(UserProductBehavior.class);
        verify(behaviors, times(2)).saveIfAbsent(saved.capture());
        assertEquals(saved.getAllValues().get(0).getEventId(), saved.getAllValues().get(1).getEventId());
    }

    @Test
    void expiredEventStillIncreasesSoldButSkipsBehavior() {
        prepare(1L);
        service.processCheckoutSucceeded(eventAt(OffsetDateTime.now(clock).minusDays(31)));
        verify(products).increaseTotalSold(5L, 5L);
        verify(behaviors, never()).saveIfAbsent(any());
    }

    @Test
    void mismatchedSnapshotRecordsNoBehavior() {
        prepare(99L);
        assertThrows(IllegalArgumentException.class, () -> service.processCheckoutSucceeded(event("COD")));
        verifyNoInteractions(products);
        verify(behaviors, never()).saveIfAbsent(any());
    }

    @Test
    void behaviorFailurePropagatesSoKafkaCanRetry() {
        prepare(1L);
        IllegalStateException failure = new IllegalStateException("DB unavailable");
        when(behaviors.saveIfAbsent(any())).thenThrow(failure);
        assertSame(failure, assertThrows(IllegalStateException.class,
                () -> service.processCheckoutSucceeded(event("COD"))));
    }
}
