package com.example.notification.application.service;

import com.example.notification.application.dto.response.CurrentUserInfoResponse;
import com.example.notification.domain.model.ChatConversation;
import com.example.notification.domain.model.ChatMessage;
import com.example.notification.application.dto.ReadChatMessagesResult;
import com.example.notification.application.dto.event.ChatConversationChangedEvent;
import java.util.List;
import java.time.Clock;
import java.time.OffsetDateTime;
import com.example.common.exception.BusinessException;
import com.example.common.error.AuthorizationError;
import com.example.notification.application.dto.response.ChatMessageResponse;
import com.example.notification.application.dto.response.ReadMessgerRepone;
import com.example.notification.application.port.in.ReadChatConversationUseCase;
import com.example.notification.application.port.in.SendChatMessageUseCase;
import com.example.notification.application.port.out.ChatMessagePublisherPort;
import com.example.notification.application.port.out.client.CurrentUserPort;
import com.example.notification.application.port.out.repo.ChatRoomRepositoryPort;
import com.example.notification.application.error.ChatError;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Slf4j
public class ChatMessageApplicationService implements SendChatMessageUseCase, ReadChatConversationUseCase {
    private final ChatRoomRepositoryPort chatRoomRepositoryPort;
    private final ChatMessagePublisherPort chatMessagePublisherPort;
    private final CurrentUserPort currentUserPort;
    private final Clock clock;

    @Override
    @Transactional
    public ReadMessgerRepone readConversationMessages(Long conversationId) {
        if (conversationId == null || conversationId <= 0) {
            throw new BusinessException(ChatError.CHAT_ROOM_INVALID);
        }
        CurrentUserInfoResponse user = currentUserPort.getUserInfo();
        if (user == null || user.userId() == null || user.userId() <= 0) {
            throw new BusinessException(AuthorizationError.UNAUTHENTICATED);
        }
        ChatConversation conversation = chatRoomRepositoryPort.findByIdForUpdate(conversationId)
                .orElseThrow(() -> new BusinessException(ChatError.CHAT_ROOM_NOT_FOUND));
        conversation.requireParticipant(user.userId());

        ReadChatMessagesResult result = chatRoomRepositoryPort.markReceivedMessagesRead(conversationId, user.userId());
        ReadMessgerRepone response = new ReadMessgerRepone(conversationId, result.lastReadMessageId());
        chatMessagePublisherPort.publishAfterCommit("/topic/chat/conversations/" + conversationId + "/read", response);
        if (result.changedMessageCount() > 0) {
            publishConversationChanged(conversationId, List.of(user.userId()));
        }
        log.info("Đã đánh dấu đọc tin nhắn: conversation_id={}, last_read_message_id={}",
                conversationId, response.lastReadMessageId());
        return response;
    }

    @Override
    @Transactional
    public ChatMessageResponse sendConversationMessage(Long conversationId, Long senderId, String content) {
        if (conversationId == null ) {
            throw new BusinessException(ChatError.CHAT_ROOM_INVALID);
        }
        ChatConversation conversation = chatRoomRepositoryPort.findByIdForUpdate(conversationId)
                .orElseThrow(() -> new BusinessException(ChatError.CHAT_ROOM_NOT_FOUND));

        conversation.addMessage(senderId, content);
        ChatMessage savedMessage = chatRoomRepositoryPort.saveMessages(conversation).getFirst();
        conversation.recordLastMessageAt(savedMessage.createdAt());
        chatRoomRepositoryPort.updateLastMessageAt(conversation);
        ChatMessageResponse response = ChatMessageResponse.from(conversationId, savedMessage, clock.getZone());

        chatMessagePublisherPort.publishAfterCommit("/topic/chat/conversations/" + conversationId, response);
        publishConversationChanged(conversationId, List.of(conversation.getCustomerId(), conversation.getShopId()));
        log.info("Đã lưu tin nhắn: conversation_id={}, message_id={}, sender_id={}",
                conversationId, savedMessage.id(), senderId);
        return response;
    }

    private void publishConversationChanged(Long conversationId, List<Long> recipientIds) {
        ChatConversationChangedEvent event = new ChatConversationChangedEvent(conversationId, OffsetDateTime.now(clock));
        // gửi cho cả shop và user cửa đoạn chat đó qua recipientId so snah với Principal của socket
        recipientIds.stream().distinct().forEach(recipientId ->
                chatMessagePublisherPort.publishToUserAfterCommit(recipientId, "/queue/chat/conversations", event));
    }
}
