package com.example.order_service.application.service;

import com.example.common.exception.BusinessException;
import com.example.order_service.application.constant.Constant;
import com.example.order_service.application.dto.event.CheckoutSucceededPayload;
import com.example.order_service.application.dto.event.OrderCancelledByShopPayload;
import com.example.order_service.application.dto.event.OrderStatusChangedPayload;
import com.example.order_service.application.dto.order.UserOrderItemView;
import com.example.order_service.application.dto.order.UserOrderView;
import com.example.order_service.application.dto.order.ShopOrderView;
import com.example.order_service.application.dto.order.ShopOrderSearch;
import com.example.order_service.application.dto.request.ShopOrderFilter;
import com.example.order_service.application.dto.response.ShopOrderResponse;
import com.example.order_service.application.dto.outbox.OutboxEventDto;
import com.example.order_service.application.dto.response.UserOrderResponse;
import com.example.order_service.application.dto.saga.OrderInventoryRollbackRequestedPayload;
import com.example.order_service.application.error.OrderError;
import com.example.order_service.application.mapper.UserOrderResponseMapper;
import com.example.order_service.application.port.in.CancelMyOrderUseCase;
import com.example.order_service.application.port.in.CancelShopOrderUseCase;
import com.example.order_service.application.port.in.AdvanceShopOrderUseCase;
import com.example.order_service.application.port.in.GetMyOrdersUseCase;
import com.example.order_service.application.port.in.GetShopOrdersUseCase;
import com.example.order_service.application.port.out.client.CurrentUserPort;
import com.example.order_service.application.port.out.outbox.OutboxPort;
import com.example.order_service.application.port.out.repo.CheckoutRepoPort;
import com.example.order_service.application.port.out.repo.OrderRepoPort;
import com.example.order_service.domain.model.Checkout;
import com.example.order_service.domain.model.Order;
import com.example.order_service.domain.model.OrderCancelReason;
import com.example.order_service.domain.model.OrderStatus;
import com.example.order_service.domain.model.PriceType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class UserOrderService implements GetMyOrdersUseCase, CancelMyOrderUseCase, GetShopOrdersUseCase,
        CancelShopOrderUseCase, AdvanceShopOrderUseCase {

    private final CurrentUserPort currentUserPort;
    private final OrderRepoPort orderRepoPort;
    private final CheckoutRepoPort checkoutRepoPort;
    private final ShopAvailabilityService shopAvailabilityService;
    private final OutboxPort outboxPort;
    private final Clock clock;

    @Override
    @Transactional(readOnly = true)
    public List<UserOrderResponse> getMyOrders(OrderStatus status) {
        Long userId = currentUserPort.getInfo().userId();
        List<UserOrderView> orders = orderRepoPort.findUserOrders(userId, status);
        Map<Long, List<UserOrderResponse.Item>> itemsByOrder = findItemsByOrder(orders);
        LocalDateTime now = LocalDateTime.now(clock);
        return orders.stream()
                .map((UserOrderView order) -> UserOrderResponseMapper.toResponse(order,
                        itemsByOrder.getOrDefault(order.orderId(), List.of()), now, clock.getZone()))
                .toList();
    }

    // Shop dùng userId của chủ shop làm shop_id; xem được cả đơn của shop đang bị khóa.
    @Override
    @Transactional(readOnly = true)
    public List<ShopOrderResponse> getShopOrders(ShopOrderFilter filter) {
        if (filter.from() != null && filter.to() != null && filter.from().isAfter(filter.to())) {
            throw new BusinessException(OrderError.INVALID_DATE_RANGE);
        }
        Long shopId = currentUserPort.getInfo().userId();
        // Bỏ dấu # khi shop dán mã đơn; từ khóa toàn số thì khớp thêm theo mã đơn.
        String keyword = filter.keyword() == null ? null : filter.keyword().trim().replaceFirst("^#", "");
        keyword = keyword == null || keyword.isEmpty() ? null : keyword;
        Long orderId = keyword != null && keyword.matches("\\d{1,18}") ? Long.valueOf(keyword) : null;
        // DB lưu LocalDateTime theo múi giờ của Clock nên đổi mốc có offset về cùng múi giờ trước khi so sánh.
        List<ShopOrderView> orders = orderRepoPort.findShopOrders(new ShopOrderSearch(shopId, filter.status(),
                filter.paymentMethod(), keyword, orderId, toZone(filter.from()), toZone(filter.to())));
        Map<Long, List<UserOrderResponse.Item>> itemsByOrder = findItemsByOrder(orders.stream().map(ShopOrderView::order).toList());
        LocalDateTime now = LocalDateTime.now(clock);
        return orders.stream()
                .map((ShopOrderView order) -> UserOrderResponseMapper.toShopResponse(order,
                        itemsByOrder.getOrDefault(order.order().orderId(), List.of()), now, clock.getZone()))
                .toList();
    }

    // Khóa dòng order trước khi kiểm tra trạng thái để không đua với thao tác xác nhận của shop.
    @Override
    @Transactional
    public void cancelMyOrder(Long orderId, OrderCancelReason reason) {
        Long userId = currentUserPort.getInfo().userId();
        Order order = orderRepoPort.findUserOrderForUpdate(orderId, userId)
                .orElseThrow(() -> new BusinessException(OrderError.ORDER_NOT_FOUND));
        // Bấm hủy lặp lại không lỗi và không hoàn kho lần nữa.
        if (order.getStatus() == OrderStatus.CANCEL) {
            return;
        }
        LocalDateTime now = LocalDateTime.now(clock);
        order.cancelByUser(reason, now);
        orderRepoPort.saveStatus(List.of(order));
        UUID eventId = requestInventoryRollback(order, now);
        log.info("User đã hủy đơn và yêu cầu hoàn tồn kho: order_id={}, checkout_id={}, reason={}, event_id={}",
                order.getId(), order.getCheckoutId(), reason, eventId);
    }

    // Kiểm tra khóa shop trước khi lock đơn để không giữ khóa DB trong lúc gọi Auth Service.
    @Override
    @Transactional
    public void cancelShopOrder(Long orderId, OrderCancelReason reason) {
        Order order = lockOrderOfUnlockedShop(orderId);
        // Đơn đã bị hủy trước đó (user, shop hay hệ thống) thì giữ nguyên, không hoàn kho và gửi mail lần nữa.
        if (order.getStatus() == OrderStatus.CANCEL) {
            return;
        }
        LocalDateTime now = LocalDateTime.now(clock);
        order.cancelByShop(reason, now);
        orderRepoPort.saveStatus(List.of(order));
        UUID rollbackEventId = requestInventoryRollback(order, now);
        UUID mailEventId = publishCancelledByShop(order, now);
        log.info("Shop đã hủy đơn: order_id={}, checkout_id={}, reason={}, rollback_event_id={}, mail_event_id={}",
                order.getId(), order.getCheckoutId(), reason, rollbackEventId, mailEventId);
    }

    // Shop chuyển đơn sang bước kế tiếp; domain quyết định bước và kiểm tra currentStatus còn đúng.
    @Override
    @Transactional
    public void advanceShopOrder(Long orderId, OrderStatus currentStatus) {
        Order order = lockOrderOfUnlockedShop(orderId);
        LocalDateTime now = LocalDateTime.now(clock);
        order.advanceByShop(currentStatus, now);
        orderRepoPort.saveStatus(List.of(order));
        Checkout checkout = findCheckout(order);
        OrderStatusChangedPayload event = OrderStatusChangedPayload.of(order, checkout, clock.getZone());
        UUID eventId = event.eventId();
        outboxPort.save(new OutboxEventDto(eventId, Constant.ORDER, order.getId().toString(), event.eventType(), event, now));
        log.info("Shop đã chuyển trạng thái đơn: order_id={}, from={}, to={}, event_id={}",
                order.getId(), currentStatus, order.getStatus(), eventId);
    }

    // Kiểm tra khóa shop trước khi lock đơn để không giữ khóa DB trong lúc gọi Auth Service.
    private Order lockOrderOfUnlockedShop(Long orderId) {
        Long shopId = currentUserPort.getInfo().userId();
        if (shopAvailabilityService.isShopLocked(shopId)) {
            throw new BusinessException(OrderError.SHOP_LOCKED);
        }
        return orderRepoPort.findShopOrderForUpdate(orderId, shopId)
                .orElseThrow(() -> new BusinessException(OrderError.ORDER_NOT_FOUND));
    }

    // Snapshot người mua để Notification gửi mail mà không phải gọi ngược Order Service.
    private Checkout findCheckout(Order order) {
        return checkoutRepoPort.findById(order.getCheckoutId())
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy checkout của đơn hàng"));
    }

    // Lấy email/tên người mua từ snapshot checkout để Notification gửi mail báo đơn bị shop hủy.
    private UUID publishCancelledByShop(Order order, LocalDateTime now) {
        Checkout checkout = findCheckout(order);
        UUID eventId = UUID.randomUUID();
        OrderCancelledByShopPayload event = new OrderCancelledByShopPayload(eventId, OrderCancelledByShopPayload.EVENT_TYPE,
                order.getId(), order.getCheckoutId(), order.getShopNameSnapshot(), checkout.getBuyerEmailSnapshot(),
                checkout.getRecipientNameSnapshot(), order.getCancelReason(), order.getTotalAmount(),
                order.getCancelledAt().atZone(clock.getZone()).toOffsetDateTime());
        outboxPort.save(new OutboxEventDto(eventId, Constant.ORDER, order.getId().toString(), event.eventType(), event, now));
        return eventId;
    }

    // Product dùng hai event id này để tìm lượng đã trừ của checkout và biết lượt bán đã được cộng chưa.
    private UUID requestInventoryRollback(Order order, LocalDateTime now) {
        CheckoutSucceededPayload succeeded = outboxPort.findCheckoutSucceeded(order.getCheckoutId())
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy event CHECKOUT_SUCCEEDED của checkout"));
        UUID eventId = UUID.randomUUID();
        OrderInventoryRollbackRequestedPayload event = new OrderInventoryRollbackRequestedPayload(eventId,
                OrderInventoryRollbackRequestedPayload.EVENT_TYPE, order.getCheckoutId(), order.getId(),
                order.getShopId(), order.getPriceType() == PriceType.SALE, succeeded.inventorySucceededEventId(),
                succeeded.eventId(), now.atZone(clock.getZone()).toOffsetDateTime());
        outboxPort.save(new OutboxEventDto(eventId, Constant.ORDER, order.getId().toString(),
                event.eventType(), event, now));
        return eventId;
    }

    private LocalDateTime toZone(OffsetDateTime value) {
        return value == null ? null : value.atZoneSameInstant(clock.getZone()).toLocalDateTime();
    }

    // Lấy item của mọi đơn trong một lần truy vấn để tránh N+1.
    private Map<Long, List<UserOrderResponse.Item>> findItemsByOrder(List<UserOrderView> orders) {
        if (orders.isEmpty()) {
            return Map.of();
        }
        List<Long> orderIds = orders.stream().map(UserOrderView::orderId).toList();
        return orderRepoPort.findUserOrderItems(orderIds).stream().collect(Collectors.groupingBy(
                UserOrderItemView::orderId, Collectors.mapping(UserOrderItemView::item, Collectors.toList())));
    }
}
