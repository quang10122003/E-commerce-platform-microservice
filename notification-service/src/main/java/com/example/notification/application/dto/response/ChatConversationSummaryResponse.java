package com.example.notification.application.dto.response;

import java.time.OffsetDateTime;

// Metadata của room trong danh sách chat, không tải toàn bộ lịch sử tin nhắn.
public record ChatConversationSummaryResponse<T>(Long conversationId, T partner, Long productId,
                                               String productName, String productImageUrl,
                                               ChatMessageResponse lastMessage, OffsetDateTime lastMessageAt,
                                               OffsetDateTime createdAt, OffsetDateTime updatedAt) {
    public record ShopInfo(Long shopId, String shopName, String shopLogoUrl) {}

    public record CustomerInfo(Long userId, String fullName) {}
}
