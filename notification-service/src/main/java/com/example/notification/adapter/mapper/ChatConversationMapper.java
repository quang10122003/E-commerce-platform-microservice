package com.example.notification.adapter.mapper;

import com.example.notification.adapter.entity.ChatConversationsEntity;
import com.example.notification.domain.model.ChatConversation;
import org.springframework.stereotype.Component;

@Component
public class ChatConversationMapper {

    public ChatConversation toDomain(ChatConversationsEntity entity) {
        return ChatConversation.restore(
                entity.getId(),
                entity.getCustomerId(),
                entity.getShopId(),
                entity.getProductId(),
                entity.getLastMessageAt(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }

    // Domain (room mới, chưa có id) -> entity mới để insert.
    public ChatConversationsEntity toNewEntity(ChatConversation domain) {
        return new ChatConversationsEntity(
                domain.getCustomerId(),
                domain.getShopId(),
                domain.getProductId());
    }
}
