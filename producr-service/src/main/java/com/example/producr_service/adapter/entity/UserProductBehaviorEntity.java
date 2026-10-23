package com.example.producr_service.adapter.entity;

import com.example.producr_service.domain.model.BehaviorType;
import com.example.producr_service.application.constant.Constant;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_product_behavior", uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_product_behavior_event", columnNames = "event_id")
}, indexes = {
        @Index(name = "idx_user_product_behavior_product", columnList = "product_id"),
        @Index(name = "idx_user_product_behavior_user_created", columnList = "user_id, created_at"),
        @Index(name = "idx_user_product_behavior_recent_view",
                columnList = "user_id, product_id, event_type, created_at"),
        @Index(name = "idx_user_product_behavior_created", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
// bảng lưu các thao tác hành đông của user
public class UserProductBehaviorEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false, length = Constant.BEHAVIOR_EVENT_ID_LENGTH)
    private String eventId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_user_product_behavior_product"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    private ProductEntity product;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false,
            columnDefinition = "ENUM('PRODUCT_VIEW', 'ADD_TO_CART', 'PURCHASE')")
    private BehaviorType eventType;

    @Column(name = "created_at", nullable = false, columnDefinition = "TIMESTAMP(6)")
    private LocalDateTime createdAt;
}
