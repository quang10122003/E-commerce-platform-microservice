package com.example.order_service.domain.model;

import com.example.common.exception.BusinessException;
import com.example.common.untill.ValidationUtils;
import com.example.order_service.domain.error.OrderDomainError;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

public final class Payment {

    private static final BigDecimal MAX_AMOUNT = new BigDecimal("9999999999999999999");

    private final Long id;
    private final Long checkoutId;
    private String paymentMethod;
    private PaymentStatus status;
    private BigDecimal amount;
    private final String currency;
    private String transactionReference;
    private String providerTransactionId;
    private String bankCode;
    private String bankTransactionId;
    private String responseCode;
    private String transactionStatus;
    private String paymentUrl;
    private LocalDateTime expiresAt;
    private LocalDateTime requestedAt;
    private LocalDateTime paidAt;
    private LocalDateTime cancelledAt;
    private String cancellationReason;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Payment(Long id, Long checkoutId, String paymentMethod, PaymentStatus status, BigDecimal amount,
            String currency, String transactionReference, String providerTransactionId, String bankCode,
            String bankTransactionId, String responseCode, String transactionStatus, String paymentUrl,
            LocalDateTime expiresAt, LocalDateTime requestedAt, LocalDateTime paidAt, LocalDateTime cancelledAt,
            String cancellationReason, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = validateOptionalId(id);
        this.checkoutId = requireId(checkoutId);
        this.paymentMethod = validateOptionalText(paymentMethod, 20);
        this.status = Objects.requireNonNull(status);
        this.amount = amount == null ? null : validateAmount(amount);
        if (!"VND".equals(currency)) {
            throw new BusinessException(OrderDomainError.INVALID_DATA);
        }
        this.currency = currency;
        this.transactionReference = validateOptionalText(transactionReference, 100);
        this.providerTransactionId = validateOptionalText(providerTransactionId, 100);
        this.bankCode = validateOptionalText(bankCode, 20);
        this.bankTransactionId = validateOptionalText(bankTransactionId, 255);
        this.responseCode = validateOptionalText(responseCode, 2);
        this.transactionStatus = validateOptionalText(transactionStatus, 2);
        this.paymentUrl = paymentUrl;
        this.createdAt = validateTime(createdAt, null);
        this.updatedAt = validateTime(updatedAt, createdAt);
        this.expiresAt = expiresAt;
        this.requestedAt = requestedAt;
        this.paidAt = paidAt;
        this.cancelledAt = cancelledAt;
        this.cancellationReason = validateOptionalText(cancellationReason, 500);
        if (expiresAt != null && !expiresAt.isAfter(createdAt)) {
            throw new BusinessException(OrderDomainError.INVALID_DATA);
        }
        if (status == PaymentStatus.PENDING && (amount == null || expiresAt == null
                || !ValidationUtils.hasText(transactionReference))) {
            throw new BusinessException(OrderDomainError.INVALID_DATA);
        }
        if (status == PaymentStatus.DONE && (amount == null || paidAt == null)) {
            throw new BusinessException(OrderDomainError.INVALID_DATA);
        }
        if ((status == PaymentStatus.CANCEL && cancelledAt == null)
                || (status != PaymentStatus.DONE && paidAt != null)
                || (status != PaymentStatus.CANCEL && cancelledAt != null)) {
            throw new BusinessException(OrderDomainError.INVALID_DATA);
        }
    }

    public static Payment create(Long checkoutId, BigDecimal amount, LocalDateTime now) {
        return new Payment(null, checkoutId, null, PaymentStatus.UNPAID, validateAmount(amount), "VND",
                null, null, null, null, null, null, null, null, null, null, null, null, now, now);
    }

    public static Payment reconstitute(Long id, Long checkoutId, String paymentMethod, PaymentStatus status,
            BigDecimal amount, String currency, String transactionReference, String providerTransactionId,
            String bankCode, String bankTransactionId, String responseCode, String transactionStatus,
            String paymentUrl, LocalDateTime expiresAt, LocalDateTime requestedAt, LocalDateTime paidAt,
            LocalDateTime cancelledAt, String cancellationReason, LocalDateTime createdAt, LocalDateTime updatedAt) {
        requireId(id);
        return new Payment(id, checkoutId, paymentMethod, status, amount, currency,
                transactionReference, providerTransactionId, bankCode, bankTransactionId,
                responseCode, transactionStatus, paymentUrl, expiresAt, requestedAt, paidAt, cancelledAt,
                cancellationReason, createdAt, updatedAt);
    }

    public void prepare(BigDecimal amount, String transactionReference, LocalDateTime expiresAt,
                        LocalDateTime now) {
        if (status != PaymentStatus.UNPAID || this.transactionReference != null) {
            throw new BusinessException(OrderDomainError.INVALID_STATE);
        }
        BigDecimal validAmount = validateAmount(amount);
        String validReference = requireText(transactionReference, 100);
        LocalDateTime preparedAt = validateTime(now, updatedAt);
        if (expiresAt == null || !expiresAt.isAfter(preparedAt)) {
            throw new BusinessException(OrderDomainError.INVALID_DATA);
        }
        this.amount = validAmount;
        this.transactionReference = validReference;
        this.expiresAt = expiresAt;
        // Provider truy vấn giao dịch theo đúng thời điểm tạo yêu cầu thanh toán này.
        this.requestedAt = preparedAt;
        updatedAt = preparedAt;
    }

    public void start(String paymentUrl, LocalDateTime now) {
        String validUrl = requireText(paymentUrl, Integer.MAX_VALUE);
        if (status == PaymentStatus.PENDING && Objects.equals(this.paymentUrl, validUrl)) {
            return;
        }
        if (status != PaymentStatus.UNPAID || amount == null || transactionReference == null
                || expiresAt == null) {
            throw new BusinessException(OrderDomainError.INVALID_STATE);
        }
        LocalDateTime startedAt = validateTime(now, updatedAt);
        if (!expiresAt.isAfter(startedAt)) {
            throw new BusinessException(OrderDomainError.INVALID_STATE);
        }
        this.paymentUrl = validUrl;
        status = PaymentStatus.PENDING;
        updatedAt = startedAt;
    }

    // Application chỉ gọi sau khi đã xác thực thông báo thanh toán qua provider.
    public void markPaid(BigDecimal receivedAmount, String providerTransactionId, String paymentMethod,
                         String bankCode, String bankTransactionId, String responseCode,
                         String transactionStatus, LocalDateTime paidAt, LocalDateTime now) {
        if (amount == null || amount.compareTo(validateAmount(receivedAmount)) != 0) {
            throw new BusinessException(OrderDomainError.TOTAL_MISMATCH);
        }
        String validTransactionId = requireText(providerTransactionId, 100);
        if (status == PaymentStatus.DONE && Objects.equals(this.providerTransactionId, validTransactionId)) {
            return;
        }
        if (status != PaymentStatus.PENDING) {
            throw new BusinessException(OrderDomainError.INVALID_STATE);
        }
        String validMethod = validateOptionalText(paymentMethod, 20);
        String validBankCode = validateOptionalText(bankCode, 20);
        String validBankTransactionId = validateOptionalText(bankTransactionId, 255);
        String validResponseCode = validateOptionalText(responseCode, 2);
        String validTransactionStatus = validateOptionalText(transactionStatus, 2);
        LocalDateTime confirmedAt = validateTime(now, updatedAt);
        LocalDateTime paymentTime = validateTime(paidAt, createdAt);
        if (paymentTime.isAfter(confirmedAt)) {
            throw new BusinessException(OrderDomainError.INVALID_DATA);
        }
        this.providerTransactionId = validTransactionId;
        this.paymentMethod = validMethod;
        this.bankCode = validBankCode;
        this.bankTransactionId = validBankTransactionId;
        this.responseCode = validResponseCode;
        this.transactionStatus = validTransactionStatus;
        this.paidAt = paymentTime;
        status = PaymentStatus.DONE;
        updatedAt = confirmedAt;
    }

    public boolean isExpiredAt(LocalDateTime time) {
        return expiresAt == null || time.isAfter(expiresAt);
    }

    // PENDING cần xác minh với provider trước khi hủy, không tự hủy theo đồng hồ local.
    public void cancel(String reason, LocalDateTime now) {
        if (status == PaymentStatus.CANCEL) {
            return;
        }
        if (status != PaymentStatus.UNPAID) {
            throw new BusinessException(OrderDomainError.INVALID_STATE);
        }
        applyCancel(reason, now);
    }

    // Chỉ gọi khi provider đã xác nhận giao dịch không thành công, không hủy theo đồng hồ local.
    public void cancelPending(String reason, LocalDateTime now) {
        if (status == PaymentStatus.CANCEL) {
            return;
        }
        if (status != PaymentStatus.PENDING) {
            throw new BusinessException(OrderDomainError.INVALID_STATE);
        }
        applyCancel(reason, now);
    }

    private void applyCancel(String reason, LocalDateTime now) {
        String validReason = requireText(reason, 500);
        LocalDateTime cancellationTime = validateTime(now, updatedAt);
        status = PaymentStatus.CANCEL;
        cancellationReason = validReason;
        cancelledAt = cancellationTime;
        updatedAt = cancellationTime;
    }

    public Long getId() {
        return id;
    }

    public Long getCheckoutId() {
        return checkoutId;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public String getTransactionReference() {
        return transactionReference;
    }

    public String getProviderTransactionId() {
        return providerTransactionId;
    }

    public String getBankCode() {
        return bankCode;
    }

    public String getBankTransactionId() {
        return bankTransactionId;
    }

    public String getResponseCode() {
        return responseCode;
    }

    public String getTransactionStatus() {
        return transactionStatus;
    }

    public String getPaymentUrl() {
        return paymentUrl;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public LocalDateTime getRequestedAt() {
        return requestedAt;
    }

    public LocalDateTime getPaidAt() {
        return paidAt;
    }

    public LocalDateTime getCancelledAt() {
        return cancelledAt;
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
