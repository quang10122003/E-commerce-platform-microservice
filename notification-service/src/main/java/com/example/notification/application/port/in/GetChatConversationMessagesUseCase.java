package com.example.notification.application.port.in;

import com.example.notification.application.dto.response.ChatMessageResponse;

import java.util.List;

public interface GetChatConversationMessagesUseCase {
    List<ChatMessageResponse> getConversationMessagesNewestFirst(Long conversationId);
}
