package com.example.order_service.order.adapter.entity;

import com.example.order_service.order.domain.model.PaymentStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment", uniqueConstraints = {
        @UniqueConstraint(name = "uk_payment_checkout", columnNames = "checkout_id"),
        @UniqueConstraint(name = "uk_payment_reference", columnNames = "transaction_reference"),
        @UniqueConstraint(name = "uk_payment_provider_transaction", columnNames = "provider_transaction_id")
})
@Getter
@Setter
@NoArgsConstructor
public class PaymentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "checkout_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_payment_checkout"))
    private CheckoutEntity checkout;

    @Column(name = "payment_method", length = 20)
    private String paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PaymentStatus status = PaymentStatus.UNPAID;

    @Column(name = "amount", precision = 19, scale = 0)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 3, columnDefinition = "CHAR(3)")
    private String currency = "VND";

    @Column(name = "transaction_reference", length = 100,
            columnDefinition = "VARCHAR(100) CHARACTER SET ascii COLLATE ascii_bin")
    private String transactionReference;

    @Column(name = "provider_transaction_id", length = 100,
            columnDefinition = "VARCHAR(100) CHARACTER SET ascii COLLATE ascii_bin")
    private String providerTransactionId;

    @Column(name = "bank_code", length = 20)
    private String bankCode;

    @Column(name = "bank_transaction_id")
    private String bankTransactionId;

    @Column(name = "response_code", length = 2)
    private String responseCode;

    @Column(name = "transaction_status", length = 2)
    private String transactionStatus;

    @Column(name = "payment_url", columnDefinition = "TEXT")
    private String paymentUrl;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "cancellation_reason", length = 500)
    private String cancellationReason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
