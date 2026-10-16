package com.example.notification.application.port.in;

public interface CheckChatConversationAccessUseCase {
    // Kiểm tra thành viên trước khi cho phép subscribe topic của room.
    void requireConversationParticipant(Long conversationId, Long userId);
}
