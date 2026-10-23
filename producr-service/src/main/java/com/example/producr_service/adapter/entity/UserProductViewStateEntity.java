package com.example.producr_service.adapter.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "user_product_view_state", uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_product_view_state_user_product", columnNames = {"user_id", "product_id"})
}, indexes = {
        @Index(name = "idx_user_product_view_state_product", columnList = "product_id"),
        @Index(name = "idx_user_product_view_state_next_allowed", columnList = "next_allowed_at")
})
@Getter
@Setter
@NoArgsConstructor
public class UserProductViewStateEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_user_product_view_state_product"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    private ProductEntity product;

    @Column(name = "next_allowed_at", nullable = false, columnDefinition = "TIMESTAMP(6)")
    private LocalDateTime nextAllowedAt;
}
