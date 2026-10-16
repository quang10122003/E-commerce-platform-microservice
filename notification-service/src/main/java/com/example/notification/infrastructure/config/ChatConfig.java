package com.example.notification.infrastructure.config;

import com.example.notification.application.constant.NotificationTime;
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
    // Lắp ghép service triển khai các input port đọc room và lịch sử tin nhắn.
    @Bean
    ChatConversationQueryService listChatConversationsUseCase(
            CurrentUserPort currentUserPort, ChatRoomRepositoryPort chatRoomRepositoryPort,
            ChatParticipantClientPort participantClientPort,
            ProductClientPort productClientPort) {
        return new ChatConversationQueryService(
                currentUserPort, chatRoomRepositoryPort, participantClientPort, productClientPort);
    }

    // Dùng múi giờ Việt Nam giống Product Service cho thời hạn vé và phiên socket.
    @Bean
    Clock chatClock() {
        return Clock.system(NotificationTime.ZONE_ID);
    }

    // Lắp ghép use case mở room qua các output port hiện có.
    @Bean
    ChatApplicationService createChatConversationsUseCase(
            ProductClientPort productClientPort, CurrentUserPort currentUserPort,
            ChatRoomRepositoryPort chatRoomRepositoryPort) {
        return new ChatApplicationService(productClientPort, currentUserPort, chatRoomRepositoryPort);
    }

    // Lắp ghép các use case gửi/đọc tin với danh tính phiên và publisher sau commit.
    @Bean
    ChatMessageApplicationService sendChatMessageUseCase(ChatRoomRepositoryPort chatRoomRepositoryPort,
                                                        ChatMessagePublisherPort chatMessagePublisherPort,
                                                        CurrentUserPort currentUserPort) {
        return new ChatMessageApplicationService(chatRoomRepositoryPort, chatMessagePublisherPort, currentUserPort);
    }
}
