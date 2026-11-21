package com.example.order_service.application.service;

import com.example.common.error.AuthorizationError;
import com.example.common.exception.BusinessException;
import com.example.order_service.application.dto.response.UserInternalInfoResponse;
import com.example.order_service.application.port.out.client.CurrentUserPort;
import com.example.order_service.application.dto.CheckoutItemDto;
import com.example.order_service.application.dto.CheckoutItemKeyDto;
import com.example.order_service.application.dto.saga.CheckoutRequestedOutboxPayload;
import com.example.order_service.application.dto.event.CheckoutSucceededPayload;
import com.example.order_service.application.dto.response.CreateCheckoutResponse;
import com.example.order_service.application.dto.request.CreateCheckoutRequest;
import com.example.order_service.application.error.OrderError;
import com.example.order_service.application.port.in.CreateCheckoutUseCase;
import com.example.order_service.application.port.in.GetCheckoutUseCase;
import com.example.order_service.application.port.in.CheckCheckoutAccessUseCase;
import com.example.order_service.application.dto.event.CheckoutFailedPayload;
import com.example.order_service.application.port.out.repo.CheckoutRepoPort;
import com.example.order_service.application.port.out.repo.PaymentRepoPort;
import com.example.order_service.domain.model.Payment;
import com.example.order_service.domain.error.OrderDomainError;
import com.example.order_service.domain.model.Checkout;
import com.example.order_service.domain.model.CheckoutPaymentMethod;
import com.example.order_service.domain.model.CheckoutStatus;
import com.example.order_service.domain.model.InboxEvent;
import com.example.order_service.domain.model.Order;
import com.example.order_service.domain.model.OrderItem;
import com.example.order_service.domain.model.PriceType;
import com.example.order_service.application.dto.checkout.CheckoutItemSnapshotDto;
import com.example.order_service.application.dto.checkout.ShopSnapshotDto;
import com.example.order_service.application.dto.saga.CheckoutInventoryRequestedPayload;
import com.example.order_service.application.dto.saga.CheckoutInventorySucceededPayload;
import com.example.order_service.application.dto.saga.CheckoutInventoryFailedPayload;
import com.example.order_service.application.dto.saga.CheckoutInventoryRollbackRequestedPayload;
import com.example.order_service.application.dto.saga.CheckoutInventoryRolledBackPayload;
import com.example.order_service.application.port.out.client.ShopSnapshotPort;
import com.example.order_service.application.port.out.repo.InboxEventPort;
import com.example.order_service.application.port.out.repo.OrderRepoPort;
import com.example.order_service.application.dto.outbox.OutboxEventDto;
import com.example.order_service.application.port.out.outbox.OutboxPort;
import com.example.order_service.application.port.out.serialization.ObjectMapperPort;
import com.example.order_service.application.constant.Constant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@RequiredArgsConstructor
@Slf4j
public class CheckoutService implements CreateCheckoutUseCase, GetCheckoutUseCase, CheckCheckoutAccessUseCase {

    private final CurrentUserPort currentUserPort;
    private final CheckoutRepoPort checkoutRepoPort;
    private final CheckoutCreationService checkoutCreationService;
    private final InboxEventPort inbox;
    private final OutboxPort outbox;
    private final OrderRepoPort orders;
    private final ShopSnapshotPort shops;
    private final ObjectMapperPort json;
    private final CheckoutCancellationService cancellation;
    private final PaymentRepoPort payments;
    private final VnpayPaymentService vnpayPaymentService;
    private final Clock clock;

    @Override
    public CreateCheckoutResponse createCheckout(CreateCheckoutRequest request, String idempotencyKey,
                                                  String clientIp) {
        UserInternalInfoResponse buyer = currentUserPort.getInfo();

        Optional<Checkout> existing = checkoutRepoPort.findByIdempotencyKey(idempotencyKey);

        if (existing.isPresent()) {
            return responseForOwner(existing.get(), buyer.userId());
        }

        // Cùng biến thể chỉ được tách thành hai dòng khi mua theo hai loại giá khác nhau.( sale và normal)
        Set<CheckoutItemKeyDto> itemKeys = new HashSet<>();
        for (CheckoutItemDto item : request.items()) {
            if (!itemKeys.add(new CheckoutItemKeyDto(item.variantId(), item.sale()))) {
                throw new BusinessException(OrderDomainError.DUPLICATE_ITEM);
            }
        }

        try {
            Checkout checkout = checkoutCreationService.create(request, idempotencyKey, clientIp, buyer);
            return new CreateCheckoutResponse(checkout.getId(), checkout.getStatus(), null, List.of(), null, null);
        } catch (BusinessException exception) {
            if (exception.getErrorCode() != OrderError.CHECKOUT_IDEMPOTENCY_CONFLICT) {
                throw exception;
            }
            // Transaction thua tranh chấp phải rollback xong trước khi đọc checkout của request thắng.
            return responseForOwner(checkoutRepoPort.findByIdempotencyKey(idempotencyKey)
                    .orElseThrow(() -> exception), buyer.userId());
        }
    }
// phát event yêu cầu prodcut service kiểm tra trừ tồn kho v.v
    @Transactional
    public void requestInventory(CheckoutRequestedOutboxPayload event) {
        Optional<Checkout> existing = checkoutRepoPort.findByIdForUpdate(event.checkoutId());
        if (existing.isEmpty() || existing.get().getStatus() != CheckoutStatus.PROCESSING) {
            return;
        }
        Checkout checkout = existing.get();
        UUID requestId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now(clock);
        CheckoutInventoryRequestedPayload request = new CheckoutInventoryRequestedPayload(requestId,
                CheckoutInventoryRequestedPayload.EVENT_TYPE, checkout.getId(), checkout.getTotalAmount(),
                event.items(), now.atZone(clock.getZone()).toOffsetDateTime());
        outbox.save(new OutboxEventDto(requestId, Constant.CHECKOUT, checkout.getId().toString(),
                request.eventType(), request, now));
        log.info("Đã lưu yêu cầu cấp hàng: event_id={}, checkout_id={}, payment_method={}",
                requestId, checkout.getId(), event.paymentMethod());
    }

    // hàm tạo order và order item khi nhận event validaton và trừ tôn kho thành công
    @Transactional
    public void processInventorySucceeded(CheckoutInventorySucceededPayload event) {
        validateResultMetadata(event.eventId(), event.checkoutId(), event.requestEventId(), event.occurredAt());
        // thêm inbox event
        if (!recordInboxEvent(event.eventId(), event.eventType(), event)) {
            return;
        }
        Checkout checkout = checkoutRepoPort.findByIdForUpdate(event.checkoutId())
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy checkout"));
        if (checkout.getStatus() != CheckoutStatus.PROCESSING) {
            return;
        }
        // Chỉ checkout VNPay có bản ghi payment từ bước tiếp nhận yêu cầu.
        Optional<Payment> payment = payments.findByCheckoutIdForUpdate(checkout.getId());
        CheckoutPaymentMethod paymentMethod = payment.isPresent() ? CheckoutPaymentMethod.VNPAY : CheckoutPaymentMethod.COD;
        List<Order> created = buildOrders(checkout.getId(), paymentMethod, event.items(), LocalDateTime.now(clock));
        BigDecimal total = created.stream().map(Order::getTotalAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        checkout.complete(total, LocalDateTime.now(clock));
        List<Long> orderIds = orders.saveAll(created);
        LocalDateTime now = LocalDateTime.now(clock);
        String paymentUrl = payment.map((Payment value) -> vnpayPaymentService.startPayment(value, checkout, now)).orElse(null);

        checkoutRepoPort.save(checkout);
        UUID eventId = UUID.randomUUID();
        CheckoutSucceededPayload payload = new CheckoutSucceededPayload(eventId,
                CheckoutSucceededPayload.EVENT_TYPE, checkout.getId(), checkout.getUserId(), checkout.getStatus(),
                paymentMethod, orderIds, checkout.getTotalAmount(),
                now.atZone(clock.getZone()).toOffsetDateTime(),
                checkout.getBuyerEmailSnapshot(), checkout.getRecipientNameSnapshot(), paymentUrl, event.eventId());

        outbox.save(new OutboxEventDto(eventId, Constant.CHECKOUT, checkout.getId().toString(),
                payload.eventType(), payload, now));
        log.info("Đã tạo đơn: event_id={}, checkout_id={}, payment_method={}, order_count={}",
                event.eventId(), checkout.getId(), payload.paymentMethod(), orderIds.size());
    }

    // hàm sử lý saga khi mà product servie validaton , trừ tôn kho , tính sale thát bại
    @Transactional
    public void processInventoryFailed(CheckoutInventoryFailedPayload event) {
        validateResultMetadata(event.eventId(), event.checkoutId(), event.requestEventId(), event.occurredAt());
        // check inbox
        if (!recordInboxEvent(event.eventId(), event.eventType(), event)) {
            return;
        }
        cancellation.cancelCheckout(event.checkoutId(), event.reasonCode(), event.reason());
    }

    // nhận event product service roll back thành công và xử lý
    @Transactional
    public void processInventoryRolledBack(CheckoutInventoryRolledBackPayload event) {
        validateResultMetadata(event.eventId(), event.checkoutId(), event.rollbackRequestEventId(), event.occurredAt());
        if (!recordInboxEvent(event.eventId(), event.eventType(), event)) {
            return;
        }
        cancellation.cancelCheckout(event.checkoutId(), event.reasonCode(), event.reason());
    }

    private void validateResultMetadata(UUID eventId, Long checkoutId, UUID requestEventId,
            OffsetDateTime occurredAt) {
        if (eventId == null || checkoutId == null || checkoutId <= 0
                || requestEventId == null || occurredAt == null) {
            throw new IllegalArgumentException("Metadata kết quả tồn kho không hợp lệ");
        }
    }
//    Ghi Inbox để tránh xử lý lặp; trả về false nếu eventId đã tồn tại.
    private boolean recordInboxEvent(UUID eventId, String eventType, Object payload) {
        InboxEvent inboxEvent = InboxEvent.create(eventId, eventType,
                json.writeValue(payload), LocalDateTime.now(clock));
        if (!inbox.tryInsert(inboxEvent)) {
            log.info("Bỏ qua event đã xử lý: event_id={}, event_type={}", eventId, eventType);
            return false;
        }
        return true;
    }

    private List<Order> buildOrders(Long checkoutId, CheckoutPaymentMethod paymentMethod,
                                    List<CheckoutItemSnapshotDto> snapshots, LocalDateTime now) {
        if (snapshots == null || snapshots.isEmpty()) {
            throw new BusinessException(OrderDomainError.INVALID_DATA);
        }
        Set<CheckoutItemKeyDto> itemKeys = new HashSet<>();
        Map<Long, Map<PriceType, List<OrderItem>>> groups = new TreeMap<>();
        for (CheckoutItemSnapshotDto snapshot : snapshots) {
            if (snapshot == null || snapshot.shopId() == null || snapshot.shopId() <= 0
                    || !itemKeys.add(new CheckoutItemKeyDto(snapshot.variantId(), snapshot.sale()))) {
                throw new BusinessException(OrderDomainError.INVALID_DATA);
            }
            PriceType priceType = snapshot.sale() ? PriceType.SALE : PriceType.NORMAL;
            OrderItem item = OrderItem.create(snapshot.productId(), snapshot.variantId(), snapshot.productName(),
                    snapshot.variantAttributes(), snapshot.imageUrl(), snapshot.quantity(), snapshot.originalUnitPrice(),
                    snapshot.unitPrice(), priceType, now);
            groups.computeIfAbsent(snapshot.shopId(), (Long id) -> new EnumMap<>(PriceType.class))
                    .computeIfAbsent(priceType, (PriceType type) -> new ArrayList<>()).add(item);
        }
        List<Order> result = new ArrayList<>();
        for (Map.Entry<Long, Map<PriceType, List<OrderItem>>> shop : groups.entrySet()) {
            ShopSnapshotDto snapshot = shops.findById(shop.getKey())
                    .orElseThrow(() -> new BusinessException(OrderError.CHECKOUT_SHOP_NOT_FOUND));
            for (Map.Entry<PriceType, List<OrderItem>> group : shop.getValue().entrySet()) {
                result.add(Order.create(checkoutId, shop.getKey(), snapshot.shopName(), snapshot.shopLogoUrl(),
                        group.getKey(), paymentMethod, group.getValue(), now));
            }
        }
        return List.copyOf(result);
    }


    //phát event Product hoàn kho sau khi transaction tạo đơn hoặc thanh toán bị lỗi đã rollback.
    @Transactional
    public void requestInventoryRollback(CheckoutInventorySucceededPayload succeeded, RuntimeException exception) {
        validateResultMetadata(succeeded.eventId(), succeeded.checkoutId(), succeeded.requestEventId(), succeeded.occurredAt());
        if (!recordInboxEvent(succeeded.eventId(), succeeded.eventType(), succeeded)) {
            return;
        }
        Checkout checkout = checkoutRepoPort.findByIdForUpdate(succeeded.checkoutId())
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy checkout"));
        if (checkout.getStatus() != CheckoutStatus.PROCESSING) {
            return;
        }
        String reasonCode = Constant.CHECKOUT_ORDER_CREATION_FAILED;
        String reason = "Không thể tạo đơn hàng hoặc khởi tạo thanh toán";
        if (exception instanceof BusinessException businessException) {
            reasonCode = businessException.getErrorCode().getCode();
            reason = businessException.getMessage();
        }
        UUID eventId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now(clock);
        CheckoutInventoryRollbackRequestedPayload event = new CheckoutInventoryRollbackRequestedPayload(eventId,
                CheckoutInventoryRollbackRequestedPayload.EVENT_TYPE, succeeded.checkoutId(), succeeded.eventId(),
                null, reasonCode, reason, now.atZone(clock.getZone()).toOffsetDateTime());
        outbox.save(new OutboxEventDto(eventId, Constant.CHECKOUT, succeeded.checkoutId().toString(),
                event.eventType(), event, now));
        log.warn("Chờ Product rollback tồn kho: event_id={}, checkout_id={}, reason_code={}",
                eventId, succeeded.checkoutId(), reasonCode);
    }

    @Override
    @Transactional(readOnly = true)
    public CreateCheckoutResponse getCheckout(Long checkoutId) {
        Checkout checkout = checkoutRepoPort.findById(checkoutId)
                .orElseThrow(() -> new BusinessException(OrderError.CHECKOUT_NOT_FOUND));
        return responseForOwner(checkout, currentUserPort.getInfo().userId());
    }

    // Checkout không tồn tại cũng trả ACCESS_DENIED để không lộ checkout id nào đang có.
    @Override
    @Transactional(readOnly = true)
    public void checkAccess(Long checkoutId) {
        Checkout checkout = checkoutRepoPort.findById(checkoutId)
                .orElseThrow(() -> new BusinessException(AuthorizationError.ACCESS_DENIED));
        requireOwner(checkout, currentUserPort.getInfo().userId());
    }

    // Lấy kết quả từ event đã ghi outbox để trả đúng dữ liệu như SSE, kể cả khi SSE đã hết hạn replay.
    private CreateCheckoutResponse responseForOwner(Checkout checkout, Long userId) {
        requireOwner(checkout, userId);
        if (checkout.getStatus() == CheckoutStatus.DONE) {
            Optional<CheckoutSucceededPayload> succeeded = outbox.findCheckoutSucceeded(checkout.getId());
            if (succeeded.isPresent()) {
                return new CreateCheckoutResponse(checkout.getId(), checkout.getStatus(),
                        succeeded.get().paymentMethod(), succeeded.get().orderIds(), succeeded.get().urlPayment(), null);
            }
        }
        if (checkout.getStatus() == CheckoutStatus.CANCEL) {
            // Không trả cancellationReason trong DB vì có thể là lỗi kỹ thuật gốc.
            String reason = outbox.findCheckoutFailed(checkout.getId()).map(CheckoutFailedPayload::reason)
                    .orElse(Constant.CHECKOUT_DEFAULT_CANCEL_REASON);
            return new CreateCheckoutResponse(checkout.getId(), checkout.getStatus(), null, List.of(), null, reason);
        }
        String paymentUrl = payments.findByCheckoutId(checkout.getId()).map(Payment::getPaymentUrl).orElse(null);
        return new CreateCheckoutResponse(checkout.getId(), checkout.getStatus(), null, List.of(), paymentUrl, null);
    }

    private void requireOwner(Checkout checkout, Long userId) {
        if (userId == null || !Objects.equals(checkout.getUserId(), userId)) {
            throw new BusinessException(AuthorizationError.ACCESS_DENIED);
        }
    }
}
