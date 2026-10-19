package com.example.notification.application.port.in;

import com.example.notification.application.dto.response.ChatMessageResponse;

public interface SendChatMessageUseCase {
    // Danh tính người gửi phải do adapter xác thực, không lấy từ payload của client.
    ChatMessageResponse sendConversationMessage(Long conversationId, Long senderId, String content);
}
