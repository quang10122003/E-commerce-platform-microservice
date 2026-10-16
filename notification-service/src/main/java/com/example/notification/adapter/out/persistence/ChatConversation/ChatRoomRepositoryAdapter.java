package com.example.notification.adapter.out.persistence.ChatConversation;

import com.example.notification.adapter.entity.ChatMessageEntity;
import com.example.common.response.PageQuery;
import com.example.common.response.PageResponse;
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

    // Lưu room bằng upsert để bảo vệ unique key khi nhiều request cùng tạo.
    @Override
    public ChatConversation save(ChatConversation conversation) {
        conversationJpa.insertIfAbsent(conversation.getCustomerId(), conversation.getShopId(),
                conversation.getProductId());
        return conversationJpa.findLockedByCustomerIdAndShopIdAndProductId(
                        conversation.getCustomerId(), conversation.getShopId(), conversation.getProductId())
                .map(chatConversationMapper::toDomain)
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy room sau khi tạo"));
    }

    // Chỉ truy vấn và chuyển entity sang domain, không quyết định tạo room.
    @Override
    public Optional<ChatConversation> findByCustomerIdAndShopIdAndProductId(
            Long customerId, Long shopId, Long productId) {
        return conversationJpa.findByCustomerIdAndShopIdAndProductId(customerId, shopId, productId)
                .map(chatConversationMapper::toDomain);
    }

    // Chỉ lấy metadata room người dùng tham gia, không truy vấn tin nhắn theo từng room.
    @Override
    public PageResponse<ChatConversation> findConversationsByCustomerId(Long customerId, PageQuery pageQuery) {
        return toConversationPage(conversationJpa.findConversationsByCustomerId(customerId,
                PageRequest.of(pageQuery.pageIndex(), pageQuery.size())));
    }

    // Chỉ đọc các room tài khoản hiện tại tiếp nhận với tư cách shop.
    @Override
    public PageResponse<ChatConversation> findConversationsByShopId(Long shopId, PageQuery pageQuery) {
        return toConversationPage(conversationJpa.findConversationsByShopId(shopId,
                PageRequest.of(pageQuery.pageIndex(), pageQuery.size())));
    }

    // Chuyển trang JPA sang kiểu phân trang dùng chung của application.
    private PageResponse<ChatConversation> toConversationPage(
            Page<ChatConversationsEntity> page) {
        return new PageResponse<>(page.getContent().stream().map(chatConversationMapper::toDomain).toList(),
                page.getNumber() + 1, page.getSize(), page.getTotalElements(), page.getTotalPages());
    }

    // Lấy tin cuối theo lô; room rỗng không xuất hiện trong kết quả tra cứu.
    @Override
    public Map<Long, ChatMessage> findLatestMessagesByConversationIds(List<Long> conversationIds) {
        if (conversationIds.isEmpty()) {
            return Map.of();
        }
        return messageJpa.findLatestMessagesByConversationIds(conversationIds).stream()
                .collect(Collectors.toMap(message -> message.getChatConversationsEntity().getId(),
                        chatMessgerMapper::toDomain));
    }

    // Tải metadata room mà không kéo theo lịch sử tin nhắn.
    @Override
    public Optional<ChatConversation> findById(Long conversationId) {
        return conversationJpa.findById(conversationId).map(chatConversationMapper::toDomain);
    }

    // Dùng khóa database để tuần tự hóa các thao tác ghi trong cùng room.
    @Override
    public Optional<ChatConversation> findByIdForUpdate(Long conversationId) {
        return conversationJpa.findLockedById(conversationId).map(chatConversationMapper::toDomain);
    }

    // Flush để nhận ID và timestamp DB trước khi ánh xạ response phát socket.
    @Override
    public List<ChatMessage> saveMessages(ChatConversation conversation) {
        ChatConversationsEntity entity = conversationJpa.findById(conversation.getId())
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy room khi lưu tin nhắn"));
        List<ChatMessageEntity> savedMessages = messageJpa.saveAllAndFlush(chatMessgerMapper.toEntities(conversation.getNewMessages(), entity));
        conversation.markMessagesPersisted();
        return chatMessgerMapper.toDomains(savedMessages);
    }

    // Không gán audit time; database tự cập nhật updated_at khi metadata thay đổi.
    @Override
    public void updateLastMessageAt(ChatConversation conversation) {
        ChatConversationsEntity entity = conversationJpa.findById(conversation.getId())
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy room khi cập nhật hoạt động"));
        entity.updateLastMessageAt(conversation.getLastMessageAt());
        conversationJpa.flush();
    }

    // Tái sử dụng mapper tin nhắn và không tải lịch sử vào aggregate room.
    @Override
    public List<ChatMessage> findMessagesByConversationId(Long conversationId) {
        return chatMessgerMapper.toDomains(
                messageJpa.findByChatConversationsEntity_IdOrderByCreatedAtAscIdAsc(conversationId));
    }

    // Chuyển toàn bộ kết quả đã sắp xếp tại database sang domain, không giới hạn số tin.
    @Override
    public List<ChatMessage> findMessagesByConversationIdNewestFirst(Long conversationId) {
        return chatMessgerMapper.toDomains(
                messageJpa.findByChatConversationsEntity_IdOrderByCreatedAtDescIdDesc(conversationId));
    }

    // Dirty checking cập nhật đúng các tin được chọn trong transaction đang khóa room.
    @Override
    public Optional<Long> markReceivedMessagesRead(Long conversationId, Long readerId) {
        List<ChatMessageEntity> unreadMessages = messageJpa
                .findByChatConversationsEntity_IdAndSenderIdNotAndReadFalseOrderByIdDesc(conversationId, readerId);
        unreadMessages.forEach(message -> message.markRead());
        messageJpa.flush();
        return messageJpa
                .findFirstByChatConversationsEntity_IdAndSenderIdNotAndReadTrueOrderByIdDesc(conversationId, readerId)
                .map(message -> message.getId());
    }
}
