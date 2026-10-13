package com.example.notification.adapter.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "chat_conversations", uniqueConstraints = @UniqueConstraint(
        name = "uk_conversation_customer_shop_product",
        columnNames = {"customer_id", "shop_id", "product_id"}))
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ChatConversationsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "shop_id", nullable = false)
    private Long shopId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "last_message_at")
    private LocalDateTime lastMessageAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    public ChatConversationsEntity(Long customerId, Long shopId, Long productId,
                                   LocalDateTime lastMessageAt, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.customerId = customerId;
        this.shopId = shopId;
        this.productId = productId;
        this.lastMessageAt = lastMessageAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Ánh xạ metadata đã được domain xử lý vào entity đang được quản lý.
    public void updateTimestamps(LocalDateTime lastMessageAt, LocalDateTime updatedAt) {
        this.lastMessageAt = lastMessageAt;
        this.updatedAt = updatedAt;
    }

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
    }

}
