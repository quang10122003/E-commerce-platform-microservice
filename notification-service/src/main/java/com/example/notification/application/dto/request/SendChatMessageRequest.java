package com.example.notification.application.dto.request;

import com.example.notification.domain.model.ChatMessage;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// Không nhận người gửi từ payload để client không giả mạo danh tính.
public record SendChatMessageRequest(
        @NotBlank @Size(max = ChatMessage.MAX_CONTENT_LENGTH) String content) {
}
