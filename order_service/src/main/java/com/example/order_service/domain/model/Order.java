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
    private String cancellationReason;
    private LocalDateTime cancelledAt;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private final List<OrderItem> items;

    private Order(Long id, Long checkoutId, Long shopId, String shopNameSnapshot,
                  String shopLogoUrlSnapshot, PriceType priceType, CheckoutPaymentMethod paymentMethod,
                  OrderStatus status, String cancellationReason, LocalDateTime cancelledAt, LocalDateTime createdAt,
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
        this.cancellationReason = validateOptionalText(cancellationReason, 500);
        this.createdAt = validateTime(createdAt, null);
        this.updatedAt = validateTime(updatedAt, createdAt);
        this.cancelledAt = cancelledAt == null ? null : validateTime(cancelledAt, createdAt);
        if (status == OrderStatus.CANCEL && cancelledAt == null) {
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
                priceType, paymentMethod, OrderStatus.PENDING_CONFIRMATION, null, null, now, now, items);
    }

    public static Order reconstitute(Long id, Long checkoutId, Long shopId, String shopNameSnapshot,
                                    String shopLogoUrlSnapshot, PriceType priceType,
                                    CheckoutPaymentMethod paymentMethod, OrderStatus status, BigDecimal totalAmount, String cancellationReason,
                                    LocalDateTime cancelledAt, LocalDateTime createdAt,
                                    LocalDateTime updatedAt, List<OrderItem> items) {
        requireId(id);
        Order order = new Order(id, checkoutId, shopId, shopNameSnapshot, shopLogoUrlSnapshot,
                priceType, paymentMethod, status, cancellationReason, cancelledAt, createdAt, updatedAt, items);
        if (order.totalAmount.compareTo(validateAmount(totalAmount)) != 0) {
            throw new BusinessException(OrderDomainError.TOTAL_MISMATCH);
        }
        return order;
    }

    public void confirm(LocalDateTime now) {
        transition(OrderStatus.PENDING_CONFIRMATION, OrderStatus.PENDING_PICKUP, now);
    }

    public void startShipping(LocalDateTime now) {
        transition(OrderStatus.PENDING_PICKUP, OrderStatus.SHIPPING, now);
    }

    public void complete(LocalDateTime now) {
        transition(OrderStatus.SHIPPING, OrderStatus.DONE, now);
    }

    public void cancel(String reason, LocalDateTime now) {
        if (status == OrderStatus.CANCEL) {
            return;
        }
        if (status != OrderStatus.PENDING_CONFIRMATION) {
            throw new BusinessException(OrderDomainError.INVALID_STATE);
        }
        String validReason = requireText(reason, 500);
        LocalDateTime cancellationTime = validateTime(now, updatedAt);
        status = OrderStatus.CANCEL;
        cancellationReason = validReason;
        cancelledAt = cancellationTime;
        updatedAt = cancellationTime;
    }

    private void transition(OrderStatus expected, OrderStatus next, LocalDateTime now) {
        if (status == next) {
            return;
        }
        if (status != expected) {
            throw new BusinessException(OrderDomainError.INVALID_STATE);
        }
        LocalDateTime transitionTime = validateTime(now, updatedAt);
        status = next;
        updatedAt = transitionTime;
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

    public String getCancellationReason() {
        return cancellationReason;
    }

    public LocalDateTime getCancelledAt() {
        return cancelledAt;
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
