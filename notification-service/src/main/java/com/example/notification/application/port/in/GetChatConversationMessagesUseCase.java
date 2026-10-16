package com.example.notification.application.port.in;

import com.example.notification.application.dto.response.ChatMessageResponse;

import java.util.List;

public interface GetChatConversationMessagesUseCase {
    // Lấy toàn bộ tin theo thời gian tạo giảm dần cho thành viên của room.
    List<ChatMessageResponse> getConversationMessagesNewestFirst(Long conversationId);
}
