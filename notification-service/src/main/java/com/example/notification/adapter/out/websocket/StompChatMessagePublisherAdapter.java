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
        executeAfterCommit(topic, () -> messagingTemplate.convertAndSend(topic, message));
    }

    @Override
    public void publishToUserAfterCommit(Long userId, String destination, Object message) {
        // Không gắn session ID để user destination được phân phối tới mọi phiên của tài khoản.
        executeAfterCommit(destination, () -> messagingTemplate.convertAndSendToUser(userId.toString(), destination, message));
    }

    private void executeAfterCommit(String destination, Runnable publishAction) {
        if (!TransactionSynchronizationManager.isActualTransactionActive()
                || !TransactionSynchronizationManager.isSynchronizationActive()) {
            throw new IllegalStateException("Phát tin chat yêu cầu transaction đang hoạt động");
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                // DB đã commit nên lỗi realtime chỉ được ghi nhận, không báo gửi tin thất bại giả.
                try {
                    publishAction.run();
                } catch (RuntimeException exception) {
                    log.error("Phát socket thất bại sau commit: destination={}", destination, exception);
                }
            }
        });
    }
}
