package com.example.notification.application.port.in;

import com.example.notification.application.dto.response.ChatMessageResponse;

public interface SendChatMessageUseCase {
    // Gửi tin bằng danh tính đã xác thực tại adapter, không nhận người gửi từ payload.
    ChatMessageResponse sendConversationMessage(Long conversationId, Long senderId, String content);
}
