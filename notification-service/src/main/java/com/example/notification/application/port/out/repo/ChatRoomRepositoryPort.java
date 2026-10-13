package com.example.notification.application.port.out.repo;

import com.example.notification.domain.model.ChatConversation;

public interface ChatRoomRepositoryPort {

    ChatConversation save(ChatConversation chatConversation);


}
