package com.example.order_service.domain.model;

import com.example.common.exception.BusinessException;
import com.example.common.untill.ValidationUtils;
import com.example.order_service.domain.error.OrderDomainError;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

public final class Checkout {

    private static final BigDecimal MAX_AMOUNT = new BigDecimal("9999999999999999999");

    private final Long id;
    private final Long userId;
    private final String buyerFullNameSnapshot;
    private final String buyerEmailSnapshot;
    private final String idempotencyKey;
    private final String shippingAddressSnapshot;
    private final String clientIp;
    private final String note;
    private final BigDecimal totalAmount;
    private CheckoutStatus status;
    private String cancellationReason;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Checkout(Long id, Long userId, String buyerFullNameSnapshot, String buyerEmailSnapshot,
                     String idempotencyKey, String shippingAddressSnapshot, String clientIp, String note,
                     BigDecimal totalAmount, CheckoutStatus status, String cancellationReason,
                     LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = validateOptionalId(id);
        this.userId = validateOptionalId(userId);
        this.buyerFullNameSnapshot = requireText(buyerFullNameSnapshot, 255);
        this.buyerEmailSnapshot = requireText(buyerEmailSnapshot, 255);
        this.idempotencyKey = requireText(idempotencyKey, 100);
        this.shippingAddressSnapshot = requireText(shippingAddressSnapshot, 1000);
        this.clientIp = validateOptionalText(clientIp, 45);
        this.note = validateOptionalText(note, 1000);
        this.totalAmount = validateAmount(totalAmount);
        this.status = Objects.requireNonNull(status);
        this.cancellationReason = validateOptionalText(cancellationReason, 500);
        this.createdAt = validateTime(createdAt, null);
        this.updatedAt = validateTime(updatedAt, createdAt);
    }

    public static Checkout create(Long userId, String buyerFullNameSnapshot, String buyerEmailSnapshot,
                                  String idempotencyKey, String shippingAddressSnapshot, String clientIp,
                                  String note, BigDecimal totalAmount, LocalDateTime now) {
        requireId(userId);
        return new Checkout(null, userId, buyerFullNameSnapshot, buyerEmailSnapshot, idempotencyKey,
                shippingAddressSnapshot, clientIp, note, totalAmount, CheckoutStatus.PROCESSING,
                null, now, now);
    }

    public static Checkout reconstitute(Long id, Long userId, String buyerFullNameSnapshot,
                                        String buyerEmailSnapshot, String idempotencyKey,
                                        String shippingAddressSnapshot, String clientIp, String note,
                                        BigDecimal totalAmount, CheckoutStatus status, String cancellationReason,
                                        LocalDateTime createdAt, LocalDateTime updatedAt) {
        requireId(id);
        return new Checkout(id, userId, buyerFullNameSnapshot, buyerEmailSnapshot, idempotencyKey,
                shippingAddressSnapshot, clientIp, note, totalAmount, status, cancellationReason,
                createdAt, updatedAt);
    }

    // DONE xác nhận xử lý checkout xong, không đồng nghĩa khách đã thanh toán.
    public void complete(BigDecimal actualTotalAmount, LocalDateTime now) {
        if (totalAmount.compareTo(validateAmount(actualTotalAmount)) != 0) {
            throw new BusinessException(OrderDomainError.TOTAL_MISMATCH);
        }
        if (status == CheckoutStatus.DONE) {
            return;
        }
        if (status != CheckoutStatus.PROCESSING) {
            throw new BusinessException(OrderDomainError.INVALID_STATE);
        }
        LocalDateTime completedAt = validateTime(now, updatedAt);
        status = CheckoutStatus.DONE;
        updatedAt = completedAt;
    }

    public void cancel(String reason, LocalDateTime now) {
        if (status == CheckoutStatus.CANCEL) {
            return;
        }
        if (status != CheckoutStatus.PROCESSING) {
            throw new BusinessException(OrderDomainError.INVALID_STATE);
        }
        String validReason = requireText(reason, 500);
        LocalDateTime cancelledAt = validateTime(now, updatedAt);
        status = CheckoutStatus.CANCEL;
        cancellationReason = validReason;
        updatedAt = cancelledAt;
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public String getBuyerFullNameSnapshot() {
        return buyerFullNameSnapshot;
    }

    public String getBuyerEmailSnapshot() {
        return buyerEmailSnapshot;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public String getShippingAddressSnapshot() {
        return shippingAddressSnapshot;
    }

    public String getClientIp() {
        return clientIp;
    }

    public String getNote() {
        return note;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public CheckoutStatus getStatus() {
        return status;
    }

    public String getCancellationReason() {
        return cancellationReason;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
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
