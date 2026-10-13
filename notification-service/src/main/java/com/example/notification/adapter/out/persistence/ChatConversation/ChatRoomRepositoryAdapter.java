package com.example.notification.adapter.out.persistence.ChatConversation;

import com.example.notification.adapter.mapper.ChatConversationMapper;
import com.example.notification.adapter.out.persistence.chatMessger.ChatMessageJpa;
import com.example.notification.application.port.out.repo.ChatRoomRepositoryPort;
import com.example.notification.domain.model.ChatConversation;
import jakarta.persistence.EntityManager;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true,level = AccessLevel.PRIVATE)
public class ChatRoomRepositoryAdapter implements ChatRoomRepositoryPort {

    private final ChatConversationJpa conversationJpa;
    private final ChatMessageJpa messageJpa;
    private final ChatConversationMapper chatConversationMapper;
    private final EntityManager entityManager;

    @Override
    public ChatConversation save(ChatConversation chatConversation) {
        return chatConversationMapper.toDomain(conversationJpa.save(chatConversationMapper.toNewEntity(chatConversation)));
    }

}
