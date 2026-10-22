package com.example.notification.application.service;

import com.example.common.exception.BusinessException;
import com.example.notification.application.dto.response.ChatConversationResponse;
import com.example.notification.application.port.in.CheckChatConversationAccessUseCase;
import com.example.notification.application.port.in.CreateChatConversationsUseCase;
import com.example.notification.application.port.out.client.CurrentUserPort;
import com.example.notification.application.port.out.client.ProductClientPort;
import com.example.notification.application.port.out.repo.ChatRoomRepositoryPort;
import com.example.notification.application.error.ChatError;
import com.example.notification.domain.model.ChatConversation;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;


@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class ChatApplicationService implements CreateChatConversationsUseCase, CheckChatConversationAccessUseCase {

    ProductClientPort productClientPort;
    CurrentUserPort currentUserPort;
    ChatRoomRepositoryPort chatRoomRepositoryPort;

    @Transactional
    public void deleteConversationsByProductId(Long productId) {
        if (productId == null || productId <= 0) {
            throw new BusinessException(ChatError.CHAT_ROOM_INVALID);
        }
        int deletedConversations = chatRoomRepositoryPort.deleteConversationsByProductId(productId);
        log.info("Dọn room của product đã xóa: product_id={}, deleted_conversations={}",
                productId, deletedConversations);
    }

    @Override
    @Transactional
    public ChatConversationResponse getOrCreateConversationByProductId(Long productId) {
        if (productId == null ) {
            throw new BusinessException(ChatError.CHAT_ROOM_INVALID);
        }
        if (!productClientPort.existsById(productId)) {
            throw new BusinessException(ChatError.CHAT_PRODUCT_NOT_FOUND);
        }
        Long shopId = productClientPort.getShopId(productId);
        Long customerId = currentUserPort.getUserInfo().userId();

        ChatConversation conversation = chatRoomRepositoryPort
                .findByCustomerIdAndShopIdAndProductId(customerId, shopId, productId)
                .orElseGet(() -> chatRoomRepositoryPort.save(
                        ChatConversation.create(customerId, shopId, productId)));
        return new ChatConversationResponse(conversation.getId());
    }

    // chekc room tồn tại hay k và user có quyền vào room k
    @Override
    @Transactional(readOnly = true)
    public void requireConversationParticipant(Long conversationId, Long userId) {
        chatRoomRepositoryPort.findById(conversationId)
                .orElseThrow(() -> new BusinessException(ChatError.CHAT_ROOM_NOT_FOUND))
                .requireParticipant(userId);
    }
}
