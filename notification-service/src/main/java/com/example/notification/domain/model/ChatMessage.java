package com.example.notification.domain.model;

import com.example.common.exception.BusinessException;
import com.example.notification.domain.error.DomainChatError;

import java.time.LocalDateTime;
import java.util.Objects;

public record ChatMessage(Long id, Long senderId, String content, boolean read,
                          LocalDateTime createdAt, LocalDateTime updatedAt) {

    public static final int MAX_CONTENT_LENGTH = 4000;

    // Tin mới chưa có timestamp vì thời gian được database sinh khi lưu.
    public ChatMessage {
        if (senderId == null || senderId <= 0 || content == null || content.isBlank()
                || content.length() > MAX_CONTENT_LENGTH) {
            throw new BusinessException(DomainChatError.CHAT_MESSAGE_INVALID);
        }
        if (id != null) {
            Objects.requireNonNull(createdAt, "Thời điểm tạo tin nhắn là bắt buộc");
            Objects.requireNonNull(updatedAt, "Thời điểm cập nhật tin nhắn là bắt buộc");
        }
    }

    static ChatMessage create(Long senderId, String content) {
        return new ChatMessage(null, senderId, content == null ? null : content.strip(), false, null, null);
    }
}
