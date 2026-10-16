package com.example.notification.application.service;

import com.example.common.exception.BusinessException;
import com.example.notification.application.dto.response.ChatConversationResponse;
import com.example.notification.application.port.in.CheckChatConversationAccessUseCase;
import com.example.notification.application.port.in.CreateChatConversationsUseCase;
import com.example.notification.application.port.out.client.CurrentUserPort;
import com.example.notification.application.port.out.client.ProductClientPort;
import com.example.notification.application.port.out.repo.ChatRoomRepositoryPort;
import com.example.notification.domain.error.DomainChatError;
import com.example.notification.domain.model.ChatConversation;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.transaction.annotation.Transactional;


@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class ChatApplicationService implements CreateChatConversationsUseCase, CheckChatConversationAccessUseCase {

    ProductClientPort productClientPort;
    CurrentUserPort currentUserPort;
    ChatRoomRepositoryPort chatRoomRepositoryPort;

    // Tìm hoặc tạo room của người đang đăng nhập, chỉ trả ID và không tải tin nhắn.
    @Override
    @Transactional
    public ChatConversationResponse getOrCreateConversationByProductId(Long productId) {
        if (productId == null ) {
            throw new BusinessException(DomainChatError.CHAT_ROOM_INVALID);
        }
        if (!productClientPort.existsById(productId)) {
            throw new BusinessException(DomainChatError.CHAT_PRODUCT_NOT_FOUND);
        }
        Long shopId = productClientPort.getShopId(productId);
        Long customerId = currentUserPort.getUserInfo().userId();

        ChatConversation conversation = chatRoomRepositoryPort
                .findByCustomerIdAndShopIdAndProductId(customerId, shopId, productId)
                .orElseGet(() -> chatRoomRepositoryPort.save(
                        ChatConversation.create(customerId, shopId, productId)));
        return new ChatConversationResponse(conversation.getId());
    }

    // Dùng danh tính đã xác thực để bảo vệ topic của từng cuộc trò chuyện.
    @Override
    @Transactional(readOnly = true)
    public void requireConversationParticipant(Long conversationId, Long userId) {
        chatRoomRepositoryPort.findById(conversationId)
                .orElseThrow(() -> new BusinessException(DomainChatError.CHAT_ROOM_NOT_FOUND))
                .requireParticipant(userId);
    }
}
