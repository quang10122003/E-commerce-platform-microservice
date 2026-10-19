package com.example.notification.domain.model;

import com.example.common.exception.BusinessException;
import com.example.notification.domain.error.DomainChatError;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class ChatConversation {

    private final Long id;
    private final Long customerId;
    private final Long shopId;
    private final Long productId;
    private final LocalDateTime createdAt;
    private LocalDateTime lastMessageAt;
    private final LocalDateTime updatedAt;
    // Collection chỉ giữ tin mới chờ lưu, không đại diện toàn bộ lịch sử của room.
    private final List<ChatMessage> newMessages = new ArrayList<>();

    private ChatConversation(Long id, Long customerId, Long shopId, Long productId, LocalDateTime lastMessageAt,
                             LocalDateTime createdAt, LocalDateTime updatedAt) {
        if (customerId == null || customerId <= 0 || shopId == null || shopId <= 0
                || productId == null || productId <= 0) {
            throw new BusinessException(DomainChatError.CHAT_ROOM_INVALID);
        }
        this.id = id;
        this.customerId = customerId;
        this.shopId = shopId;
        this.productId = productId;
        this.lastMessageAt = lastMessageAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public Long getShopId() {
        return shopId;
    }

    public Long getProductId() {
        return productId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getLastMessageAt() {
        return lastMessageAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public static ChatConversation create(Long customerId, Long shopId, Long productId) {
        return new ChatConversation(null, customerId, shopId, productId, null, null, null);
    }

    public static ChatConversation restore(Long id, Long customerId, Long shopId, Long productId, LocalDateTime lastMessageAt,
                                           LocalDateTime createdAt, LocalDateTime updatedAt) {
        return new ChatConversation(Objects.requireNonNull(id), customerId, shopId, productId,
                lastMessageAt, Objects.requireNonNull(createdAt), Objects.requireNonNull(updatedAt));
    }

    public void requireParticipant(Long senderId) {
        if (!customerId.equals(senderId) && !shopId.equals(senderId)) {
            throw new BusinessException(DomainChatError.CHAT_ACCESS_DENIED);
        }
    }

    // Timestamp persistence do database sinh, không tự gán khi tạo tin trong domain.
    public ChatMessage addMessage(Long senderId, String content) {
        requireParticipant(senderId);
        ChatMessage message = ChatMessage.create(senderId, content);
        newMessages.add(message);
        return message;
    }

    // Hoạt động room phải dùng thời điểm database đã ghi cho tin, tránh lệch thứ tự hiển thị.
    public void recordLastMessageAt(LocalDateTime messageCreatedAt) {
        Objects.requireNonNull(messageCreatedAt, "Thời điểm tin đã lưu là bắt buộc");
        if (lastMessageAt == null || messageCreatedAt.isAfter(lastMessageAt)) {
            lastMessageAt = messageCreatedAt;
        }
    }

    // Adapter gọi sau khi lưu xong để không insert lặp nếu aggregate bị save lần nữa.
    public void markMessagesPersisted() {
        newMessages.clear();
    }

    public List<ChatMessage> getNewMessages() {
        return List.copyOf(newMessages);
    }
}
