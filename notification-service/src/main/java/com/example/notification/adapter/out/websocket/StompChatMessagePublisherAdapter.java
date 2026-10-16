package com.example.notification.adapter.out.websocket;

import com.example.notification.application.port.out.ChatMessagePublisherPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Component
@RequiredArgsConstructor
@Slf4j
public class StompChatMessagePublisherAdapter implements ChatMessagePublisherPort {

    private final SimpMessagingTemplate messagingTemplate;

    // Đăng ký callback transaction để không phát tin chưa commit hoặc đã rollback.
    @Override
    public void publishAfterCommit(String topic, Object message) {
        if (!TransactionSynchronizationManager.isActualTransactionActive()
                || !TransactionSynchronizationManager.isSynchronizationActive()) {
            throw new IllegalStateException("Phát tin chat yêu cầu transaction đang hoạt động");
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                // DB đã commit nên lỗi realtime chỉ được ghi nhận, không báo gửi tin thất bại giả.
                try {
                    messagingTemplate.convertAndSend(topic, message);
                } catch (RuntimeException exception) {
                    log.error("Phát socket thất bại sau commit: topic={}", topic, exception);
                }
            }
        });
    }
}
