package com.example.notification.application.port.out;

import com.example.notification.application.dto.WsTicketData;

import java.time.Duration;
import java.util.Optional;

public interface WsTicketStorePort {
    // Không ghi đè vé đang tồn tại để tránh thay danh tính của vé đã cấp.
    boolean saveWsTicketIfAbsent(String ticket, WsTicketData data, Duration ttl);

    // Lấy và xóa trong một thao tác atomic, không cho hai kết nối dùng cùng vé.
    Optional<WsTicketData> getAndDeleteWsTicket(String ticket);
}
