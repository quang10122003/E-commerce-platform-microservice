package com.example.notification.domain.model;

import com.example.common.exception.BusinessException;
import com.example.notification.domain.error.DomainChatError;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class ChatConversation {

    @Getter
    private final Long id;
    // Lưu khách hàng, tài khoản người bán và sản phẩm định danh room.
    @Getter
    private final Long customerId;
    @Getter
    private final Long shopId;
    @Getter
    private final Long productId;
    @Getter
    private final LocalDateTime createdAt;
    // Lưu thời điểm tin mới nhất và cập nhật aggregate.
    @Getter
    private LocalDateTime lastMessageAt;
    @Getter
    private LocalDateTime updatedAt;
    // Chỉ giữ các tin mới của thao tác hiện tại, không tải toàn bộ lịch sử.
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
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
    }

    // Tạo room mới từ bộ khóa nghiệp vụ, chưa có tin nhắn hoặc ID persistence.
    public static ChatConversation create(Long customerId, Long shopId, Long productId, LocalDateTime now) {
        return new ChatConversation(null, customerId, shopId, productId, null, now, now);
    }

    // Khôi phục metadata của room từ persistence mà không thay đổi nghiệp vụ.
    public static ChatConversation restore(Long id, Long customerId, Long shopId, Long productId, LocalDateTime lastMessageAt,
                                           LocalDateTime createdAt, LocalDateTime updatedAt) {
        return new ChatConversation(Objects.requireNonNull(id), customerId, shopId, productId,
                lastMessageAt, createdAt, updatedAt);
    }

    // Chỉ khách hàng và tài khoản người bán của room được gửi tin.
    public void requireParticipant(Long senderId) {
        if (!customerId.equals(senderId) && !shopId.equals(senderId)) {
            throw new BusinessException(DomainChatError.CHAT_ACCESS_DENIED);
        }
    }

    public ChatMessage addMessage(Long senderId, String content, LocalDateTime now) {
        Objects.requireNonNull(now, "Thời điểm gửi tin là bắt buộc");
        requireParticipant(senderId);
        ChatMessage message = ChatMessage.create(senderId, content, now);
        newMessages.add(message);
        if (lastMessageAt == null || now.isAfter(lastMessageAt)) {
            lastMessageAt = now;
        }
        if (now.isAfter(updatedAt)) {
            updatedAt = now;
        }
        return message;
    }

    // Adapter gọi sau khi lưu xong để không insert lặp nếu aggregate bị save lần nữa.
    public void markMessagesPersisted() {
        newMessages.clear();
    }

    public List<ChatMessage> getNewMessages() {
        return List.copyOf(newMessages);
    }
}
