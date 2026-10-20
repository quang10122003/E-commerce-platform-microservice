package com.example.notification.application.service;

import com.example.notification.application.dto.response.CurrentUserInfoResponse;
import com.example.notification.domain.model.ChatMessage;
import com.example.common.error.AuthorizationError;
import com.example.common.exception.BusinessException;
import com.example.common.response.PageQuery;
import com.example.common.response.PageResponse;
import com.example.common.response.ScrollResponse;
import com.example.notification.application.dto.response.ChatConversationSummaryResponse.CustomerInfo;
import com.example.notification.application.dto.response.ChatConversationSummaryResponse.ShopInfo;
import com.example.notification.application.dto.response.ChatConversationSummaryResponse;
import com.example.notification.application.dto.response.ChatMessageResponse;
import com.example.notification.application.dto.response.ChatProductInfo;
import com.example.notification.application.port.in.ListChatConversationsUseCase;
import com.example.notification.application.port.in.GetChatConversationMessagesUseCase;
import com.example.notification.application.port.out.client.ChatParticipantClientPort;
import com.example.notification.application.port.out.client.CurrentUserPort;
import com.example.notification.application.port.out.client.ProductClientPort;
import com.example.notification.application.port.out.repo.ChatRoomRepositoryPort;
import com.example.notification.domain.model.ChatConversation;
import com.example.notification.application.error.ChatError;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.time.Clock;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class ChatConversationQueryService implements ListChatConversationsUseCase, GetChatConversationMessagesUseCase {
    private final CurrentUserPort currentUserPort;
    private final ChatRoomRepositoryPort chatRoomRepositoryPort;
    private final ChatParticipantClientPort chatParticipantClientPort;
    private final ProductClientPort productClientPort;
    private final Clock clock;

    // Đọc lịch sử không đồng nghĩa đã đọc tin; trạng thái đọc có use case riêng.
    @Override
    @Transactional(readOnly = true)
    public ScrollResponse<ChatMessageResponse> getConversationMessagesNewestFirst(Long conversationId, String cursor) {
        if (conversationId == null || conversationId <= 0) {
            throw new BusinessException(ChatError.CHAT_ROOM_INVALID);
        }
        Long userId = getAuthenticatedUserIdOrThrow();
        chatRoomRepositoryPort.findById(conversationId)
                .orElseThrow(() -> new BusinessException(ChatError.CHAT_ROOM_NOT_FOUND))
                .requireParticipant(userId);
        ScrollResponse<ChatMessage> messages = chatRoomRepositoryPort.findMessagesByConversationIdNewestFirst(conversationId, cursor);
        List<ChatMessageResponse> items = messages.items().stream()
                .map(message -> ChatMessageResponse.from(conversationId, message, clock.getZone()))
                .toList();
        return new ScrollResponse<>(items, messages.nextCursor(), messages.hasNext());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ChatConversationSummaryResponse<ShopInfo>> listCurrentCustomerConversations(PageQuery pageQuery) {
        Long readerId = getAuthenticatedUserIdOrThrow();
        PageResponse<ChatConversation> page = chatRoomRepositoryPort.findConversationsByCustomerId(readerId, pageQuery);
        List<Long> shopIds = page.items().stream().map(ChatConversation::getShopId).distinct().toList();
        Map<Long, ShopInfo> shops = shopIds.isEmpty() ? Map.<Long, ShopInfo>of() : chatParticipantClientPort.findShopInfosByIds(shopIds);
        return buildConversationSummaryPage(page, readerId, room -> shops.getOrDefault(room.getShopId(),
                new ShopInfo(room.getShopId(), null, null)));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ChatConversationSummaryResponse<CustomerInfo>> listCurrentShopConversations(PageQuery pageQuery) {
        Long readerId = getAuthenticatedUserIdOrThrow();
        PageResponse<ChatConversation> page = chatRoomRepositoryPort.findConversationsByShopId(readerId, pageQuery);
        List<Long> customerIds = page.items().stream().map(ChatConversation::getCustomerId).distinct().toList();
        Map<Long, String> names = customerIds.isEmpty() ? Map.<Long, String>of() : chatParticipantClientPort.findCustomerNamesByIds(customerIds);
        return buildConversationSummaryPage(page, readerId,
                room -> new CustomerInfo(room.getCustomerId(), names.get(room.getCustomerId())));
    }

    @Transactional(readOnly = true)
    public Map<Long, Integer> countUnreadMessages(List<Long> conversationIds, Long readerId) {
        if (readerId == null || readerId <= 0) {
            throw new BusinessException(AuthorizationError.UNAUTHENTICATED);
        }
        if (conversationIds.isEmpty()) {
            return Map.of();
        }
        return chatRoomRepositoryPort.countUnreadMessagesByConversationIds(conversationIds, readerId).entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> Math.toIntExact(entry.getValue())));
    }

    // Không nhận ID tài khoản từ client để tránh giả mạo danh tính khi đọc room.
    private Long getAuthenticatedUserIdOrThrow() {
        CurrentUserInfoResponse user = currentUserPort.getUserInfo();
        if (user == null || user.userId() == null || user.userId() <= 0) {
            throw new BusinessException(AuthorizationError.UNAUTHENTICATED);
        }
        return user.userId();
    }

    // Metadata bị xóa không được làm mất room hoặc thay đổi tổng số phần tử của trang.
    private <T> PageResponse<ChatConversationSummaryResponse<T>> buildConversationSummaryPage(
            PageResponse<ChatConversation> page, Long readerId, Function<ChatConversation, T> partnerMapper) {
        if (page.items().isEmpty()) {
            return new PageResponse<>(List.of(), page.page(), page.pageSize(), page.totalItems(), page.totalPages());
        }
        List<Long> roomIds = page.items().stream().map(ChatConversation::getId).toList();
        Map<Long, Integer> unreadMessageCounts = countUnreadMessages(roomIds, readerId);
        List<Long> productIds = page.items().stream().map(ChatConversation::getProductId).distinct().toList();
        Map<Long, ChatMessage> messages = chatRoomRepositoryPort.findLatestMessagesByConversationIds(roomIds);
        Map<Long, ChatProductInfo> products = productClientPort.findProductInfosByIds(productIds).stream()
                .collect(Collectors.toMap(ChatProductInfo::productId, Function.identity()));
        List<ChatConversationSummaryResponse<T>> items = page.items().stream().map(room -> {
            ChatProductInfo product = products.get(room.getProductId());
            ChatMessage message = messages.get(room.getId());
            return new ChatConversationSummaryResponse<>(room.getId(), partnerMapper.apply(room), room.getProductId(),
                    product == null ? null : product.name(), product == null ? null : product.imageUrl(),
                    message == null ? null : ChatMessageResponse.from(room.getId(), message, clock.getZone()),
                    room.getLastMessageAt() == null ? null : room.getLastMessageAt().atZone(clock.getZone()).toOffsetDateTime(),
                    room.getCreatedAt() == null ? null : room.getCreatedAt().atZone(clock.getZone()).toOffsetDateTime(),
                    room.getUpdatedAt() == null ? null : room.getUpdatedAt().atZone(clock.getZone()).toOffsetDateTime(),
                    unreadMessageCounts.getOrDefault(room.getId(), 0));
        }).toList();
        return new PageResponse<>(items, page.page(), page.pageSize(), page.totalItems(), page.totalPages());
    }
}
