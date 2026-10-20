package com.example.notification.adapter.out.persistence.chatMessger;

import com.example.notification.adapter.entity.ChatMessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

public interface ChatMessageJpa extends JpaRepository<ChatMessageEntity, Long> {
    @Query("""
            select message from ChatMessageEntity message
            where message.chatConversationsEntity.id in :conversationIds
              and not exists (
                select newer.id from ChatMessageEntity newer
                where newer.chatConversationsEntity.id = message.chatConversationsEntity.id
                  and (newer.createdAt > message.createdAt
                    or (newer.createdAt = message.createdAt and newer.id > message.id))
              )
            """)
    List<ChatMessageEntity> findLatestMessagesByConversationIds(@Param("conversationIds") List<Long> conversationIds);

    @Query("""
            select conversation.id as conversationId, count(message.id) as unreadCount
            from ChatMessageEntity message
            join message.chatConversationsEntity conversation
            where conversation.id in :conversationIds
              and message.read = false
              and message.senderId != :readerId
              and ((conversation.customerId = :readerId and message.senderId = conversation.shopId)
                or (conversation.shopId = :readerId and message.senderId = conversation.customerId))
            group by conversation.id
            """)
    List<UnreadMessageCount> countUnreadMessagesByConversationIds(
            @Param("conversationIds") List<Long> conversationIds, @Param("readerId") Long readerId);

    interface UnreadMessageCount {
        Long getConversationId();

        Long getUnreadCount();
    }

    List<ChatMessageEntity> findByChatConversationsEntity_IdOrderByCreatedAtAscIdAsc(Long conversationId);

    List<ChatMessageEntity> findByChatConversationsEntity_IdOrderByCreatedAtDescIdDesc(Long conversationId, Pageable pageable);

    // ID phân định các tin cùng timestamp để chuyển trang không bị lặp hoặc bỏ sót.
    @Query("""
            select message from ChatMessageEntity message
            where message.chatConversationsEntity.id = :conversationId
              and (message.createdAt < :createdAt
                or (message.createdAt = :createdAt and message.id < :messageId))
            order by message.createdAt desc, message.id desc
            """)
    List<ChatMessageEntity> findMessagesBeforeCursor(@Param("conversationId") Long conversationId,
                                                    @Param("createdAt") LocalDateTime createdAt,
                                                    @Param("messageId") Long messageId, Pageable pageable);

    List<ChatMessageEntity> findByChatConversationsEntity_IdAndSenderIdNotAndReadFalseOrderByIdDesc(
            Long conversationId, Long readerId);

    // Giữ mốc đọc ổn định khi client gọi lại hoặc không còn tin chưa đọc.
    Optional<ChatMessageEntity> findFirstByChatConversationsEntity_IdAndSenderIdNotAndReadTrueOrderByIdDesc(
            Long conversationId, Long readerId);
}
