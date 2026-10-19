package com.example.notification.adapter.out.persistence.ChatConversation;

import com.example.notification.adapter.entity.ChatMessageEntity;
import com.example.common.response.PageQuery;
import com.example.common.response.PageResponse;
import com.example.notification.application.dto.ReadChatMessagesResult;
import com.example.notification.adapter.entity.ChatConversationsEntity;
import com.example.notification.adapter.mapper.ChatConversationMapper;
import com.example.notification.adapter.mapper.ChatMessgerMapper;
import com.example.notification.adapter.out.persistence.chatMessger.ChatMessageJpa;
import com.example.notification.application.port.out.repo.ChatRoomRepositoryPort;
import com.example.notification.domain.model.ChatConversation;
import com.example.notification.domain.model.ChatMessage;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true,level = AccessLevel.PRIVATE)
public class ChatRoomRepositoryAdapter implements ChatRoomRepositoryPort {

    private final ChatConversationJpa conversationJpa;
    private final ChatMessageJpa messageJpa;
    private final ChatConversationMapper chatConversationMapper;
    private final ChatMessgerMapper chatMessgerMapper;

    // Upsert bảo vệ unique key khi nhiều request cùng mở một room.
    @Override
    public ChatConversation save(ChatConversation conversation) {
        conversationJpa.insertIfAbsent(conversation.getCustomerId(), conversation.getShopId(),
                conversation.getProductId());
        return conversationJpa.findLockedByCustomerIdAndShopIdAndProductId(
                        conversation.getCustomerId(), conversation.getShopId(), conversation.getProductId())
                .map(chatConversationMapper::toDomain)
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy room sau khi tạo"));
    }

    @Override
    public Optional<ChatConversation> findByCustomerIdAndShopIdAndProductId(
            Long customerId, Long shopId, Long productId) {
        return conversationJpa.findByCustomerIdAndShopIdAndProductId(customerId, shopId, productId)
                .map(chatConversationMapper::toDomain);
    }

    // Không tải tin nhắn theo từng room để tránh truy vấn N+1 khi phân trang.
    @Override
    public PageResponse<ChatConversation> findConversationsByCustomerId(Long customerId, PageQuery pageQuery) {
        return toConversationPage(conversationJpa.findConversationsByCustomerId(customerId,
                PageRequest.of(pageQuery.pageIndex(), pageQuery.size())));
    }

    @Override
    public PageResponse<ChatConversation> findConversationsByShopId(Long shopId, PageQuery pageQuery) {
        return toConversationPage(conversationJpa.findConversationsByShopId(shopId,
                PageRequest.of(pageQuery.pageIndex(), pageQuery.size())));
    }

    private PageResponse<ChatConversation> toConversationPage(
            Page<ChatConversationsEntity> page) {
        return new PageResponse<>(page.getContent().stream().map(chatConversationMapper::toDomain).toList(),
                page.getNumber() + 1, page.getSize(), page.getTotalElements(), page.getTotalPages());
    }

    // Room chưa có tin nhắn sẽ không có phần tử trong kết quả tra cứu.
    @Override
    public Map<Long, ChatMessage> findLatestMessagesByConversationIds(List<Long> conversationIds) {
        if (conversationIds.isEmpty()) {
            return Map.of();
        }
        return messageJpa.findLatestMessagesByConversationIds(conversationIds).stream()
                .collect(Collectors.toMap(message -> message.getChatConversationsEntity().getId(),
                        chatMessgerMapper::toDomain));
    }

    @Override
    public Map<Long, Long> countUnreadMessagesByConversationIds(List<Long> conversationIds, Long readerId) {
        if (conversationIds.isEmpty()) {
            return Map.of();
        }
        return messageJpa.countUnreadMessagesByConversationIds(conversationIds, readerId).stream()
                .collect(Collectors.toMap(ChatMessageJpa.UnreadMessageCount::getConversationId,
                        ChatMessageJpa.UnreadMessageCount::getUnreadCount));
    }

    @Override
    public Optional<ChatConversation> findById(Long conversationId) {
        return conversationJpa.findById(conversationId).map(chatConversationMapper::toDomain);
    }

    // Khóa phải bao trùm thao tác ghi để tin nhắn và metadata room không cập nhật chéo nhau.
    @Override
    public Optional<ChatConversation> findByIdForUpdate(Long conversationId) {
        return conversationJpa.findLockedById(conversationId).map(chatConversationMapper::toDomain);
    }

    // Phải flush trước khi dựng payload socket để có ID và timestamp do database sinh.
    @Override
    public List<ChatMessage> saveMessages(ChatConversation conversation) {
        ChatConversationsEntity entity = conversationJpa.findById(conversation.getId())
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy room khi lưu tin nhắn"));
        List<ChatMessageEntity> savedMessages = messageJpa.saveAllAndFlush(chatMessgerMapper.toEntities(conversation.getNewMessages(), entity));
        conversation.markMessagesPersisted();
        return chatMessgerMapper.toDomains(savedMessages);
    }

    // Database tự quản lý audit time; không gán lại khi cập nhật metadata.
    @Override
    public void updateLastMessageAt(ChatConversation conversation) {
        ChatConversationsEntity entity = conversationJpa.findById(conversation.getId())
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy room khi cập nhật hoạt động"));
        entity.updateLastMessageAt(conversation.getLastMessageAt());
        conversationJpa.flush();
    }

    @Override
    public List<ChatMessage> findMessagesByConversationId(Long conversationId) {
        return chatMessgerMapper.toDomains(
                messageJpa.findByChatConversationsEntity_IdOrderByCreatedAtAscIdAsc(conversationId));
    }

    @Override
    public List<ChatMessage> findMessagesByConversationIdNewestFirst(Long conversationId) {
        return chatMessgerMapper.toDomains(
                messageJpa.findByChatConversationsEntity_IdOrderByCreatedAtDescIdDesc(conversationId));
    }

    // Dirty checking cần transaction đang giữ khóa room để lưu trạng thái đọc nhất quán.
    @Override
    public ReadChatMessagesResult markReceivedMessagesRead(Long conversationId, Long readerId) {
        List<ChatMessageEntity> unreadMessages = messageJpa
                .findByChatConversationsEntity_IdAndSenderIdNotAndReadFalseOrderByIdDesc(conversationId, readerId);
        unreadMessages.forEach(message -> message.markRead());
        messageJpa.flush();
        Long lastReadMessageId = messageJpa
                .findFirstByChatConversationsEntity_IdAndSenderIdNotAndReadTrueOrderByIdDesc(conversationId, readerId)
                .map(message -> message.getId()).orElse(null);
        return new ReadChatMessagesResult(lastReadMessageId, unreadMessages.size());
    }
}
