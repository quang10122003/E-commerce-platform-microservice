package com.example.notification.domain.model;

import com.example.common.exception.BusinessException;
import com.example.notification.domain.error.DomainChatError;

import java.time.LocalDateTime;
import java.util.Objects;

// Tin nhắn bất biến, chỉ được tạo qua hành vi gửi tin của room.
public record ChatMessage(Long id, Long senderId, String content, boolean read,
                          LocalDateTime createdAt, LocalDateTime updatedAt) {

    public static final int MAX_CONTENT_LENGTH = 4000;

    // Tin đã lưu phải có timestamp; tin mới chờ database sinh thời gian.
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

    // Tạo tin nhắn chưa đọc và chuẩn hóa khoảng trắng ở hai đầu.
    static ChatMessage create(Long senderId, String content) {
        return new ChatMessage(null, senderId, content == null ? null : content.strip(), false, null, null);
    }
}
