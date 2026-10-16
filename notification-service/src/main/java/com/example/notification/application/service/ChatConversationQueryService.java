package com.example.notification.application.service;

import com.example.notification.application.dto.response.CurrentUserInfoResponse;
import com.example.notification.domain.model.ChatMessage;
import com.example.common.error.AuthorizationError;
import com.example.common.exception.BusinessException;
import com.example.common.response.PageQuery;
import com.example.common.response.PageResponse;
import com.example.notification.application.constant.NotificationTime;
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
import com.example.notification.domain.error.DomainChatError;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class ChatConversationQueryService implements ListChatConversationsUseCase, GetChatConversationMessagesUseCase {
    private final CurrentUserPort currentUserPort;
    private final ChatRoomRepositoryPort chatRoomRepositoryPort;
    private final ChatParticipantClientPort chatParticipantClientPort;
    private final ProductClientPort productClientPort;

    // Kiểm tra thành viên trước khi đọc toàn bộ lịch sử, không đánh dấu đã đọc.
    @Override
    @Transactional(readOnly = true)
    public List<ChatMessageResponse> getConversationMessagesNewestFirst(Long conversationId) {
        if (conversationId == null || conversationId <= 0) {
            throw new BusinessException(DomainChatError.CHAT_ROOM_INVALID);
        }
        Long userId = getAuthenticatedUserIdOrThrow();
        chatRoomRepositoryPort.findById(conversationId)
                .orElseThrow(() -> new BusinessException(DomainChatError.CHAT_ROOM_NOT_FOUND))
                .requireParticipant(userId);
        return chatRoomRepositoryPort.findMessagesByConversationIdNewestFirst(conversationId).stream()
                .map(message -> ChatMessageResponse.from(conversationId, message))
                .toList();
    }

    // Chỉ đọc room bên mua và thông tin hiển thị của shop trong trang hiện tại.
    @Override
    @Transactional(readOnly = true)
    public PageResponse<ChatConversationSummaryResponse<ShopInfo>> listCurrentCustomerConversations(PageQuery pageQuery) {
        PageResponse<ChatConversation> page = chatRoomRepositoryPort.findConversationsByCustomerId(getAuthenticatedUserIdOrThrow(), pageQuery);
        List<Long> shopIds = page.items().stream().map(ChatConversation::getShopId).distinct().toList();
        Map<Long, ShopInfo> shops = shopIds.isEmpty() ? Map.<Long, ShopInfo>of() : chatParticipantClientPort.findShopInfosByIds(shopIds);
        return buildConversationSummaryPage(page, room -> shops.getOrDefault(room.getShopId(),
                new ShopInfo(room.getShopId(), null, null)));
    }

    // Chỉ đọc room bên bán và tên các khách đối thoại trong trang hiện tại.
    @Override
    @Transactional(readOnly = true)
    public PageResponse<ChatConversationSummaryResponse<CustomerInfo>> listCurrentShopConversations(PageQuery pageQuery) {
        PageResponse<ChatConversation> page = chatRoomRepositoryPort.findConversationsByShopId(getAuthenticatedUserIdOrThrow(), pageQuery);
        List<Long> customerIds = page.items().stream().map(ChatConversation::getCustomerId).distinct().toList();
        Map<Long, String> names = customerIds.isEmpty() ? Map.<Long, String>of() : chatParticipantClientPort.findCustomerNamesByIds(customerIds);
        return buildConversationSummaryPage(page, room -> new CustomerInfo(room.getCustomerId(), names.get(room.getCustomerId())));
    }

    // Dùng danh tính từ flow xác thực hiện có, không nhận ID tài khoản từ client.
    private Long getAuthenticatedUserIdOrThrow() {
        CurrentUserInfoResponse user = currentUserPort.getUserInfo();
        if (user == null || user.userId() == null || user.userId() <= 0) {
            throw new BusinessException(AuthorizationError.UNAUTHENTICATED);
        }
        return user.userId();
    }

    // Ghép tin cuối và sản phẩm theo lô; metadata bị xóa không làm mất room hay sai tổng trang.
    private <T> PageResponse<ChatConversationSummaryResponse<T>> buildConversationSummaryPage(
            PageResponse<ChatConversation> page, Function<ChatConversation, T> partnerMapper) {
        if (page.items().isEmpty()) {
            return new PageResponse<>(List.of(), page.page(), page.pageSize(), page.totalItems(), page.totalPages());
        }
        List<Long> roomIds = page.items().stream().map(ChatConversation::getId).toList();
        List<Long> productIds = page.items().stream().map(ChatConversation::getProductId).distinct().toList();
        Map<Long, ChatMessage> messages = chatRoomRepositoryPort.findLatestMessagesByConversationIds(roomIds);
        Map<Long, ChatProductInfo> products = productClientPort.findProductInfosByIds(productIds).stream()
                .collect(Collectors.toMap(ChatProductInfo::productId, Function.identity()));
        List<ChatConversationSummaryResponse<T>> items = page.items().stream().map(room -> {
            ChatProductInfo product = products.get(room.getProductId());
            ChatMessage message = messages.get(room.getId());
            return new ChatConversationSummaryResponse<>(room.getId(), partnerMapper.apply(room), room.getProductId(),
                    product == null ? null : product.name(), product == null ? null : product.imageUrl(),
                    message == null ? null : ChatMessageResponse.from(room.getId(), message),
                    NotificationTime.toOffsetDateTime(room.getLastMessageAt()),
                    NotificationTime.toOffsetDateTime(room.getCreatedAt()),
                    NotificationTime.toOffsetDateTime(room.getUpdatedAt()));
        }).toList();
        return new PageResponse<>(items, page.page(), page.pageSize(), page.totalItems(), page.totalPages());
    }
}
