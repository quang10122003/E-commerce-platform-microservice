package com.example.notification.infrastructure.config;

import com.example.notification.application.port.out.ChatMessagePublisherPort;
import com.example.notification.application.port.out.client.ChatParticipantClientPort;
import com.example.notification.application.port.out.client.CurrentUserPort;
import com.example.notification.application.port.out.client.ProductClientPort;
import com.example.notification.application.port.out.repo.ChatRoomRepositoryPort;
import com.example.notification.application.service.ChatApplicationService;
import com.example.notification.application.service.ChatConversationQueryService;
import com.example.notification.application.service.ChatMessageApplicationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class ChatConfig {
    @Bean
    ChatConversationQueryService listChatConversationsUseCase(
            CurrentUserPort currentUserPort, ChatRoomRepositoryPort chatRoomRepositoryPort,
            ChatParticipantClientPort participantClientPort,
            ProductClientPort productClientPort, Clock clock) {
        return new ChatConversationQueryService(
                currentUserPort, chatRoomRepositoryPort, participantClientPort, productClientPort, clock);
    }

    @Bean
    ChatApplicationService createChatConversationsUseCase(
            ProductClientPort productClientPort, CurrentUserPort currentUserPort,
            ChatRoomRepositoryPort chatRoomRepositoryPort) {
        return new ChatApplicationService(productClientPort, currentUserPort, chatRoomRepositoryPort);
    }

    @Bean
    ChatMessageApplicationService sendChatMessageUseCase(ChatRoomRepositoryPort chatRoomRepositoryPort,
                                                        ChatMessagePublisherPort chatMessagePublisherPort,
                                                        CurrentUserPort currentUserPort, Clock clock) {
        return new ChatMessageApplicationService(chatRoomRepositoryPort, chatMessagePublisherPort, currentUserPort, clock);
    }
}
