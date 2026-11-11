package com.example.order_service.domain.model;

import com.example.common.exception.BusinessException;
import com.example.common.untill.ValidationUtils;
import com.example.order_service.domain.error.OrderDomainError;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

public final class Order {

    private static final BigDecimal MAX_AMOUNT = new BigDecimal("9999999999999999999");

    private final Long id;
    private final Long checkoutId;
    private final Long shopId;
    private final String shopNameSnapshot;
    private final String shopLogoUrlSnapshot;
    private final PriceType priceType;
    private final CheckoutPaymentMethod paymentMethod;
    private OrderStatus status;
    private final BigDecimal totalAmount;
    private OrderCancelledBy cancelledBy;
    private OrderCancelReason cancelReason;
    private LocalDateTime cancelledAt;
    private LocalDateTime confirmedAt;
    private LocalDateTime shippedAt;
    private LocalDateTime deliveredAt;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private final List<OrderItem> items;

    private Order(Long id, Long checkoutId, Long shopId, String shopNameSnapshot,
                  String shopLogoUrlSnapshot, PriceType priceType, CheckoutPaymentMethod paymentMethod,
                  OrderStatus status, OrderCancelledBy cancelledBy, OrderCancelReason cancelReason,
                  LocalDateTime cancelledAt, LocalDateTime confirmedAt, LocalDateTime shippedAt, LocalDateTime deliveredAt,
                  LocalDateTime createdAt,
                  LocalDateTime updatedAt, List<OrderItem> items) {
        if (items == null || items.isEmpty()) {
            throw new BusinessException(OrderDomainError.ITEMS_REQUIRED);
        }
        this.priceType = Objects.requireNonNull(priceType);
        this.paymentMethod = Objects.requireNonNull(paymentMethod);
        this.items = List.copyOf(items);
        Set<Long> variantIds = new HashSet<>();
        BigDecimal total = BigDecimal.ZERO;
        for (OrderItem item : this.items) {
            if (item.getPriceType() != priceType) {
                throw new BusinessException(OrderDomainError.PRICE_TYPE_MISMATCH);
            }
            if (item.getProductVariantId() != null && !variantIds.add(item.getProductVariantId())) {
                throw new BusinessException(OrderDomainError.DUPLICATE_ITEM);
            }
            total = total.add(item.getTotalAmount());
        }
        this.totalAmount = validateAmount(total);
        this.id = validateOptionalId(id);
        this.checkoutId = requireId(checkoutId);
        this.shopId = validateOptionalId(shopId);
        this.shopNameSnapshot = validateOptionalText(shopNameSnapshot, 255);
        this.shopLogoUrlSnapshot = shopLogoUrlSnapshot;
        this.status = Objects.requireNonNull(status);
        this.cancelledBy = cancelledBy;
        this.cancelReason = cancelReason;
        this.createdAt = validateTime(createdAt, null);
        this.updatedAt = validateTime(updatedAt, createdAt);
        this.cancelledAt = cancelledAt == null ? null : validateTime(cancelledAt, createdAt);
        this.confirmedAt = confirmedAt == null ? null : validateTime(confirmedAt, createdAt);
        this.shippedAt = shippedAt == null ? null : validateTime(shippedAt, createdAt);
        this.deliveredAt = deliveredAt == null ? null : validateTime(deliveredAt, createdAt);
        if (status == OrderStatus.CANCEL && (cancelledAt == null || cancelledBy == null)) {
            throw new BusinessException(OrderDomainError.INVALID_DATA);
        }
    }

    public static Order create(Long checkoutId, Long shopId, String shopNameSnapshot,
                               String shopLogoUrlSnapshot, PriceType priceType,
                               CheckoutPaymentMethod paymentMethod, List<OrderItem> items, LocalDateTime now) {
        requireId(shopId);
        requireText(shopNameSnapshot, 255);
        if (items != null && items.stream().anyMatch(item -> item == null
                || item.getProductId() == null || item.getProductVariantId() == null)) {
            throw new BusinessException(OrderDomainError.INVALID_DATA);
        }
        return new Order(null, checkoutId, shopId, shopNameSnapshot, shopLogoUrlSnapshot,
                priceType, paymentMethod, OrderStatus.PENDING_CONFIRMATION, null, null, null, null, null, null, now, now, items);
    }

    public static Order reconstitute(Long id, Long checkoutId, Long shopId, String shopNameSnapshot,
                                    String shopLogoUrlSnapshot, PriceType priceType,
                                    CheckoutPaymentMethod paymentMethod, OrderStatus status, BigDecimal totalAmount, OrderCancelledBy cancelledBy,
                                    OrderCancelReason cancelReason, LocalDateTime cancelledAt, LocalDateTime confirmedAt,
                                    LocalDateTime shippedAt, LocalDateTime deliveredAt, LocalDateTime createdAt,
                                    LocalDateTime updatedAt, List<OrderItem> items) {
        requireId(id);
        Order order = new Order(id, checkoutId, shopId, shopNameSnapshot, shopLogoUrlSnapshot,
                priceType, paymentMethod, status, cancelledBy, cancelReason, cancelledAt, confirmedAt, shippedAt, deliveredAt,
                createdAt, updatedAt, items);
        if (order.totalAmount.compareTo(validateAmount(totalAmount)) != 0) {
            throw new BusinessException(OrderDomainError.TOTAL_MISMATCH);
        }
        return order;
    }

    // Ghi mốc xác nhận cho cả shop xác nhận COD lẫn hệ thống tự xác nhận khi VNPay thanh toán xong.
    public void confirm(LocalDateTime now) {
        if (transition(OrderStatus.PENDING_CONFIRMATION, OrderStatus.PENDING_PICKUP, now)) {
            confirmedAt = updatedAt;
        }
    }

    public void startShipping(LocalDateTime now) {
        if (transition(OrderStatus.PENDING_PICKUP, OrderStatus.SHIPPING, now)) {
            shippedAt = updatedAt;
        }
    }

    // Shop chuyển đơn sang bước kế tiếp do domain quyết định; currentStatus là trạng thái UI đang thấy.
    public void advanceByShop(OrderStatus currentStatus, LocalDateTime now) {
        if (currentStatus == null) {
            throw new BusinessException(OrderDomainError.INVALID_DATA);
        }
        if (status != currentStatus) {
            throw new BusinessException(OrderDomainError.STATUS_CHANGED);
        }
        switch (status) {
            case PENDING_CONFIRMATION -> {
                // Đơn VNPay được hệ thống tự xác nhận khi thanh toán xong, shop không xác nhận tay.
                if (paymentMethod != CheckoutPaymentMethod.COD) {
                    throw new BusinessException(OrderDomainError.AUTO_CONFIRMED_BY_PAYMENT);
                }
                confirm(now);
            }
            case PENDING_PICKUP -> startShipping(now);
            // SHIPPING sang DONE do job tự động; DONE và CANCEL không còn bước kế tiếp.
            default -> throw new BusinessException(OrderDomainError.INVALID_STATE);
        }
    }

    // Job gọi khi đơn đang giao đủ thời gian; ghi mốc giao thành công cho dòng thời gian.
    public void complete(LocalDateTime now) {
        if (transition(OrderStatus.SHIPPING, OrderStatus.DONE, now)) {
            deliveredAt = updatedAt;
        }
    }

    // Hủy được khi shop chưa giao cho đơn vị vận chuyển; reason null khi hệ thống tự hủy.
    public void cancel(OrderCancelledBy actor, OrderCancelReason reason, LocalDateTime now) {
        if (status == OrderStatus.CANCEL) {
            return;
        }
        if (status != OrderStatus.PENDING_CONFIRMATION && status != OrderStatus.PENDING_PICKUP) {
            throw new BusinessException(OrderDomainError.INVALID_STATE);
        }
        OrderCancelledBy validActor = Objects.requireNonNull(actor);
        LocalDateTime cancellationTime = validateTime(now, updatedAt);
        status = OrderStatus.CANCEL;
        cancelledBy = validActor;
        cancelReason = reason;
        cancelledAt = cancellationTime;
        updatedAt = cancellationTime;
    }

    public void cancelByUser(OrderCancelReason reason, LocalDateTime now) {
        cancelByParticipant(OrderCancelledBy.USER, reason, now);
    }

    public void cancelByShop(OrderCancelReason reason, LocalDateTime now) {
        cancelByParticipant(OrderCancelledBy.SHOP, reason, now);
    }

    // User và shop chỉ hủy đơn COD: đơn VNPay đã trả tiền cần luồng hoàn tiền chưa hỗ trợ.
    private void cancelByParticipant(OrderCancelledBy actor, OrderCancelReason reason, LocalDateTime now) {
        if (status == OrderStatus.CANCEL) {
            return;
        }
        if (status != OrderStatus.PENDING_CONFIRMATION && status != OrderStatus.PENDING_PICKUP) {
            throw new BusinessException(OrderDomainError.INVALID_STATE);
        }
        if (paymentMethod != CheckoutPaymentMethod.COD) {
            throw new BusinessException(OrderDomainError.ONLINE_PAYMENT_NOT_CANCELLABLE);
        }
        if (reason == null) {
            throw new BusinessException(OrderDomainError.INVALID_DATA);
        }
        if (!reason.allowedFor(actor)) {
            throw new BusinessException(OrderDomainError.CANCEL_REASON_NOT_ALLOWED);
        }
        cancel(actor, reason, now);
    }

    // Trả về true khi trạng thái thực sự thay đổi để caller ghi mốc thời gian của bước.
    private boolean transition(OrderStatus expected, OrderStatus next, LocalDateTime now) {
        if (status == next) {
            return false;
        }
        if (status != expected) {
            throw new BusinessException(OrderDomainError.INVALID_STATE);
        }
        LocalDateTime transitionTime = validateTime(now, updatedAt);
        status = next;
        updatedAt = transitionTime;
        return true;
    }

    public Long getId() {
        return id;
    }

    public Long getCheckoutId() {
        return checkoutId;
    }

    public Long getShopId() {
        return shopId;
    }

    public String getShopNameSnapshot() {
        return shopNameSnapshot;
    }

    public String getShopLogoUrlSnapshot() {
        return shopLogoUrlSnapshot;
    }

    public PriceType getPriceType() {
        return priceType;
    }

    public CheckoutPaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public OrderCancelledBy getCancelledBy() {
        return cancelledBy;
    }

    public OrderCancelReason getCancelReason() {
        return cancelReason;
    }

    public LocalDateTime getCancelledAt() {
        return cancelledAt;
    }

    public LocalDateTime getConfirmedAt() {
        return confirmedAt;
    }

    public LocalDateTime getShippedAt() {
        return shippedAt;
    }

    public LocalDateTime getDeliveredAt() {
        return deliveredAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    private static BigDecimal validateAmount(BigDecimal value) {
        if (value == null || value.signum() <= 0 || value.compareTo(MAX_AMOUNT) > 0
                || value.stripTrailingZeros().scale() > 0) {
            throw new BusinessException(OrderDomainError.INVALID_AMOUNT);
        }
        return value;
    }

    private static Long requireId(Long value) {
        if (value == null || value <= 0) {
            throw new BusinessException(OrderDomainError.INVALID_DATA);
        }
        return value;
    }

    private static Long validateOptionalId(Long value) {
        return value == null ? null : requireId(value);
    }

    private static String requireText(String value, int maxLength) {
        if (!ValidationUtils.hasText(value) || value.length() > maxLength) {
            throw new BusinessException(OrderDomainError.INVALID_DATA);
        }
        return value;
    }

    private static String validateOptionalText(String value, int maxLength) {
        if (value != null && value.length() > maxLength) {
            throw new BusinessException(OrderDomainError.INVALID_DATA);
        }
        return value;
    }

    private static LocalDateTime validateTime(LocalDateTime value, LocalDateTime earliest) {
        if (value == null || (earliest != null && value.isBefore(earliest))) {
            throw new BusinessException(OrderDomainError.INVALID_DATA);
        }
        return value;
    }
}
