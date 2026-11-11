package com.example.order_service.adapter.entity;

import com.example.order_service.domain.model.CheckoutPaymentMethod;
import com.example.order_service.domain.model.OrderStatus;
import com.example.order_service.domain.model.OrderCancelledBy;
import com.example.order_service.domain.model.OrderCancelReason;
import com.example.order_service.domain.model.PriceType;
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
@Table(name = "orders", uniqueConstraints =
        @UniqueConstraint(name = "uk_orders_checkout_shop_price_type",
                columnNames = {"checkout_id", "shop_id", "price_type"}))
@Getter
@Setter
@NoArgsConstructor
public class OrderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "checkout_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_orders_checkout"))
    private CheckoutEntity checkout;

    @Column(name = "shop_id")
    private Long shopId;

    @Column(name = "shop_name_snapshot")
    private String shopNameSnapshot;

    @Column(name = "shop_logo_url_snapshot", columnDefinition = "TEXT")
    private String shopLogoUrlSnapshot;

    @Enumerated(EnumType.STRING)
    @Column(name = "price_type", nullable = false)
    private PriceType priceType;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private CheckoutPaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatus status = OrderStatus.PENDING_CONFIRMATION;

    @Column(name = "total_amount", nullable = false, precision = 19, scale = 0)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "cancelled_by")
    private OrderCancelledBy cancelledBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "cancel_reason")
    private OrderCancelReason cancelReason;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    @Column(name = "shipped_at")
    private LocalDateTime shippedAt;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY)
    @BatchSize(size = 50)
    private List<OrderItemEntity> items = new ArrayList<>();
}
