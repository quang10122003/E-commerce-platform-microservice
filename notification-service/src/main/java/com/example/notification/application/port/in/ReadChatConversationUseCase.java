package com.example.notification.application.port.in;

import com.example.notification.application.dto.response.ReadMessgerRepone;

public interface ReadChatConversationUseCase {

    // Đánh dấu tin nhận được trong room bằng danh tính phiên hiện tại.
    ReadMessgerRepone readConversationMessages(Long conversationId);
}
