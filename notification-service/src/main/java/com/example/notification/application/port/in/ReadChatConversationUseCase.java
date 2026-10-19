package com.example.notification.application.port.in;

import com.example.notification.application.dto.response.ReadMessgerRepone;

public interface ReadChatConversationUseCase {

    ReadMessgerRepone readConversationMessages(Long conversationId);
}
