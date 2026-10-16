package com.example.notification.adapter.out.persistence.chatMessger;

import com.example.notification.adapter.entity.ChatMessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChatMessageJpa extends JpaRepository<ChatMessageEntity, Long> {
    // Chọn tin mới nhất theo thời gian và ID cho cả trang room trong một truy vấn.
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

    // ID phân xử thứ tự khi nhiều tin có cùng thời điểm tạo.
    List<ChatMessageEntity> findByChatConversationsEntity_IdOrderByCreatedAtAscIdAsc(Long conversationId);

    // Giữ truy vấn mới riêng để không đổi thứ tự lịch sử của API mở room.
    List<ChatMessageEntity> findByChatConversationsEntity_IdOrderByCreatedAtDescIdDesc(Long conversationId);

    // Chỉ tải tin chưa đọc trong room, loại trừ tin do người đọc tự gửi.
    List<ChatMessageEntity> findByChatConversationsEntity_IdAndSenderIdNotAndReadFalseOrderByIdDesc(
            Long conversationId, Long readerId);

    // Giữ mốc đọc ổn định khi client gọi lại hoặc room không còn tin chưa đọc.
    Optional<ChatMessageEntity> findFirstByChatConversationsEntity_IdAndSenderIdNotAndReadTrueOrderByIdDesc(
            Long conversationId, Long readerId);
}
