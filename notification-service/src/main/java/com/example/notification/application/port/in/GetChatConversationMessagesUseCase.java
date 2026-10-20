package com.example.notification.application.port.in;

import com.example.notification.application.dto.response.ChatMessageResponse;

import com.example.common.response.ScrollResponse;

public interface GetChatConversationMessagesUseCase {
    ScrollResponse<ChatMessageResponse> getConversationMessagesNewestFirst(Long conversationId, String cursor);
}
