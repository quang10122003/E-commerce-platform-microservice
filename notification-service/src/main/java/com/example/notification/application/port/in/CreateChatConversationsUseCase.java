package com.example.notification.application.port.in;

import com.example.notification.application.dto.response.ChatConversationResponse;

public interface CreateChatConversationsUseCase {

    ChatConversationResponse getOrCreateConversationByProductId(Long productId);
}
