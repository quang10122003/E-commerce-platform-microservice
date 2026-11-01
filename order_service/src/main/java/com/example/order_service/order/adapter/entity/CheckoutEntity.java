package com.example.order_service.order.adapter.entity;

import com.example.order_service.order.domain.model.CheckoutStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "checkout", uniqueConstraints =
        @UniqueConstraint(name = "uk_checkout_idempotency_key", columnNames = "idempotency_key"))
@Getter
@Setter
@NoArgsConstructor
public class CheckoutEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "buyer_full_name_snapshot", nullable = false)
    private String buyerFullNameSnapshot;

    @Column(name = "buyer_email_snapshot", nullable = false)
    private String buyerEmailSnapshot;

    @Column(name = "idempotency_key", nullable = false, length = 100,
            columnDefinition = "VARCHAR(100) CHARACTER SET ascii COLLATE ascii_bin")
    private String idempotencyKey;

    @Column(name = "shipping_address_snapshot", nullable = false, length = 1000)
    private String shippingAddressSnapshot;

    @Column(name = "client_ip", length = 45)
    private String clientIp;

    @Column(name = "note", length = 1000)
    private String note;

    @Column(name = "total_amount", nullable = false, precision = 19, scale = 0)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private CheckoutStatus status = CheckoutStatus.PROCESSING;

    @Column(name = "cancellation_reason", length = 500)
    private String cancellationReason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "checkout", fetch = FetchType.LAZY)
    @BatchSize(size = 50)
    private List<OrderEntity> orders = new ArrayList<>();
}
