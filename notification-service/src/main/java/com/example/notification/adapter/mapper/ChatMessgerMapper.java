package com.example.notification.adapter.mapper;

import com.example.notification.adapter.entity.ChatConversationsEntity;
import com.example.notification.adapter.entity.ChatMessageEntity;
import com.example.notification.domain.model.ChatMessage;
import org.springframework.stereotype.Component;

import java.util.List;
@Component
public class ChatMessgerMapper {
    // Entity -> domain. Không đọc chatConversationsEntity nên không kích hoạt lazy load.
    public ChatMessage toDomain(ChatMessageEntity entity) {
        return new ChatMessage(
                entity.getId(),
                entity.getSenderId(),
                entity.getContent(),
                entity.isRead(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }

    public List<ChatMessage> toDomains(List<ChatMessageEntity> entities) {
        return entities.stream().map(this::toDomain).toList();
    }

    // Domain -> entity, gắn với room entity đang có trong tay.
    public ChatMessageEntity toEntity(ChatMessage message, ChatConversationsEntity conversation) {
        return new ChatMessageEntity(
                conversation,
                message.senderId(),
                message.content(),
                message.read());
    }

    public List<ChatMessageEntity> toEntities(List<ChatMessage> messages, ChatConversationsEntity conversation) {
        return messages.stream().map(m -> toEntity(m, conversation)).toList();
    }
}
