package com.example.notification.application.port.out;

public interface ChatMessagePublisherPort {

    // Phát payload tới topic được chỉ định khi transaction đã commit thành công.
    void publishAfterCommit(String topic, Object message);
}
