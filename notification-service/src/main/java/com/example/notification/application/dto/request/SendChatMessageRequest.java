package com.example.notification.application.dto.request;

import com.example.notification.domain.model.ChatMessage;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// Chỉ nhận nội dung; người gửi được xác định từ phiên đăng nhập.
public record SendChatMessageRequest(
        @NotBlank @Size(max = ChatMessage.MAX_CONTENT_LENGTH) String content) {
}
