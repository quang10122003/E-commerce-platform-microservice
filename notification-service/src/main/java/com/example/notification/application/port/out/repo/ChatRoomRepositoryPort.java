package com.example.notification.application.port.out.repo;

import com.example.common.response.PageQuery;
import com.example.common.response.PageResponse;
import com.example.notification.application.dto.ReadChatMessagesResult;
import com.example.notification.domain.model.ChatConversation;
import com.example.notification.domain.model.ChatMessage;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ChatRoomRepositoryPort {

    ChatConversation save(ChatConversation chatConversation);

    Optional<ChatConversation> findById(Long conversationId);

    // Khóa room để các thao tác gửi đồng thời cập nhật metadata tuần tự.
    Optional<ChatConversation> findByIdForUpdate(Long conversationId);

    // Cần timestamp do database sinh trong cùng transaction trước khi trả tin đã lưu.
    List<ChatMessage> saveMessages(ChatConversation conversation);

    // Không ghi createdAt hoặc updatedAt vì database quản lý audit time.
    void updateLastMessageAt(ChatConversation conversation);

    Optional<ChatConversation> findByCustomerIdAndShopIdAndProductId(Long customerId, Long shopId, Long productId);

    PageResponse<ChatConversation> findConversationsByCustomerId(Long customerId, PageQuery pageQuery);

    PageResponse<ChatConversation> findConversationsByShopId(Long shopId, PageQuery pageQuery);

    // Đọc theo lô để danh sách room không kéo toàn bộ lịch sử hoặc phát sinh N+1.
    Map<Long, ChatMessage> findLatestMessagesByConversationIds(List<Long> conversationIds);

    Map<Long, Long> countUnreadMessagesByConversationIds(List<Long> conversationIds, Long readerId);

    List<ChatMessage> findMessagesByConversationId(Long conversationId);

    List<ChatMessage> findMessagesByConversationIdNewestFirst(Long conversationId);

    ReadChatMessagesResult markReceivedMessagesRead(Long conversationId, Long readerId);
}
