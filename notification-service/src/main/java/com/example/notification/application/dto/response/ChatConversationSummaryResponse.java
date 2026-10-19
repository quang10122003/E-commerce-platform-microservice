package com.example.notification.application.dto.response;

import java.time.OffsetDateTime;

public record ChatConversationSummaryResponse<T>(Long conversationId, T partner, Long productId,
                                               String productName, String productImageUrl,
                                               ChatMessageResponse lastMessage, OffsetDateTime lastMessageAt,
                                               OffsetDateTime createdAt, OffsetDateTime updatedAt,
                                               int unreadMessageCount) {
    public record ShopInfo(Long shopId, String shopName, String shopLogoUrl) {}

    public record CustomerInfo(Long userId, String fullName) {}
}
