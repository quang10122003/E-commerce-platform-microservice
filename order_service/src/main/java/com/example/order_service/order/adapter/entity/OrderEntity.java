package com.example.order_service.order.adapter.entity;

import com.example.order_service.order.domain.model.OrderStatus;
import com.example.order_service.order.domain.model.PriceType;
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
    @Column(name = "status", nullable = false)
    private OrderStatus status = OrderStatus.PENDING_CONFIRMATION;

    @Column(name = "total_amount", nullable = false, precision = 19, scale = 0)
    private BigDecimal totalAmount;

    @Column(name = "cancellation_reason", length = 500)
    private String cancellationReason;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY)
    @BatchSize(size = 50)
    private List<OrderItemEntity> items = new ArrayList<>();
}
