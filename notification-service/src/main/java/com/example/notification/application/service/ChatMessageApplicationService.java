package com.example.notification.application.service;

import com.example.notification.application.dto.response.CurrentUserInfoResponse;
import com.example.notification.domain.model.ChatConversation;
import com.example.notification.domain.model.ChatMessage;
import java.util.Optional;
import com.example.common.exception.BusinessException;
import com.example.common.error.AuthorizationError;
import com.example.notification.application.dto.response.ChatMessageResponse;
import com.example.notification.application.dto.response.ReadMessgerRepone;
import com.example.notification.application.port.in.ReadChatConversationUseCase;
import com.example.notification.application.port.in.SendChatMessageUseCase;
import com.example.notification.application.port.out.ChatMessagePublisherPort;
import com.example.notification.application.port.out.client.CurrentUserPort;
import com.example.notification.application.port.out.repo.ChatRoomRepositoryPort;
import com.example.notification.domain.error.DomainChatError;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Slf4j
public class ChatMessageApplicationService implements SendChatMessageUseCase, ReadChatConversationUseCase {
    private final ChatRoomRepositoryPort chatRoomRepositoryPort;
    private final ChatMessagePublisherPort chatMessagePublisherPort;
    private final CurrentUserPort currentUserPort;

    // Kiểm tra phiên và thành viên trước khi đánh dấu tin nhận, rồi phát mốc đọc sau commit.
    @Override
    @Transactional
    public ReadMessgerRepone readConversationMessages(Long conversationId) {
        if (conversationId == null || conversationId <= 0) {
            throw new BusinessException(DomainChatError.CHAT_ROOM_INVALID);
        }
        CurrentUserInfoResponse user = currentUserPort.getUserInfo();
        if (user == null || user.userId() == null || user.userId() <= 0) {
            throw new BusinessException(AuthorizationError.UNAUTHENTICATED);
        }
        ChatConversation conversation = chatRoomRepositoryPort.findByIdForUpdate(conversationId)
                .orElseThrow(() -> new BusinessException(DomainChatError.CHAT_ROOM_NOT_FOUND));
        conversation.requireParticipant(user.userId());

        Optional<Long> lastReadMessageId = chatRoomRepositoryPort.markReceivedMessagesRead(conversationId, user.userId());
        ReadMessgerRepone response = new ReadMessgerRepone(conversationId, lastReadMessageId.orElse(null));
        chatMessagePublisherPort.publishAfterCommit("/topic/chat/conversations/" + conversationId + "/read", response);
        log.info("Đã đánh dấu đọc tin nhắn: conversation_id={}, last_read_message_id={}",
                conversationId, response.lastReadMessageId());
        return response;
    }

    // Điều phối kiểm tra thành viên, lưu tin và đăng ký phát socket sau commit.
    @Override
    @Transactional
    public ChatMessageResponse sendConversationMessage(Long conversationId, Long senderId, String content) {
        if (conversationId == null ) {
            throw new BusinessException(DomainChatError.CHAT_ROOM_INVALID);
        }
        ChatConversation conversation = chatRoomRepositoryPort.findByIdForUpdate(conversationId)
                .orElseThrow(() -> new BusinessException(DomainChatError.CHAT_ROOM_NOT_FOUND));

        conversation.addMessage(senderId, content);
        ChatMessage savedMessage = chatRoomRepositoryPort.saveMessages(conversation).getFirst();
        conversation.recordLastMessageAt(savedMessage.createdAt());
        chatRoomRepositoryPort.updateLastMessageAt(conversation);
        ChatMessageResponse response = ChatMessageResponse.from(conversationId, savedMessage);

        chatMessagePublisherPort.publishAfterCommit("/topic/chat/conversations/" + conversationId, response);
        log.info("Đã lưu tin nhắn: conversation_id={}, message_id={}, sender_id={}",
                conversationId, savedMessage.id(), senderId);
        return response;
    }
}
