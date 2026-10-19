package com.example.notification.application.port.in;

public interface CheckChatConversationAccessUseCase {
    void requireConversationParticipant(Long conversationId, Long userId);
}
