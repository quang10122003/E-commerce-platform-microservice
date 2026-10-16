package com.example.notification.application.dto.response;

import com.example.notification.application.constant.NotificationTime;
import com.example.notification.domain.model.ChatMessage;

import java.time.OffsetDateTime;

// Dữ liệu tin nhắn trả về khi mở cuộc trò chuyện.
public record ChatMessageResponse(Long id, Long conversationId, Long senderId, String content,
                                  boolean read, OffsetDateTime createdAt, OffsetDateTime updatedAt) {
    // Dùng chung ánh xạ response cho lịch sử, kết quả gửi và sự kiện socket.
    public static ChatMessageResponse from(Long conversationId, ChatMessage message) {
        return new ChatMessageResponse(message.id(), conversationId, message.senderId(), message.content(),
                message.read(), NotificationTime.toOffsetDateTime(message.createdAt()),
                NotificationTime.toOffsetDateTime(message.updatedAt()));
    }
}
