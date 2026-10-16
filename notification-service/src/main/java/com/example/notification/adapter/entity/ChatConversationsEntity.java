package com.example.notification.adapter.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_conversations", uniqueConstraints = @UniqueConstraint(
        name = "uk_conversation_customer_shop_product",
        columnNames = {"customer_id", "shop_id", "product_id"}))
@Getter
@NoArgsConstructor
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

    @Generated(event = EventType.INSERT)
    @Column(name = "created_at", nullable = false, insertable = false, updatable = false,
            columnDefinition = "DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6)")
    private LocalDateTime createdAt;

    @Generated(event = {EventType.INSERT, EventType.UPDATE})
    @Column(name = "updated_at", nullable = false, insertable = false, updatable = false,
            columnDefinition = "DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6)")
    private LocalDateTime updatedAt;
    
    public ChatConversationsEntity(Long customerId, Long shopId, Long productId) {
        this.customerId = customerId;
        this.shopId = shopId;
        this.productId = productId;
    }

    // Ánh xạ metadata đã được domain xử lý vào entity đang được quản lý.
    public void updateLastMessageAt(LocalDateTime lastMessageAt) {
        this.lastMessageAt = lastMessageAt;
    }

}
