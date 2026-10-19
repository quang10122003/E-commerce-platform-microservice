package com.example.notification.application.port.out;

public interface ChatMessagePublisherPort {

    // Chỉ phát sau commit để client không nhận dữ liệu đã rollback.
    void publishAfterCommit(String topic, Object message);

    void publishToUserAfterCommit(Long userId, String destination, Object message);
}
