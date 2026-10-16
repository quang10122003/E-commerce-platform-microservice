package com.example.notification.application.port.in;

import com.example.notification.application.dto.WsTicketIdentity;

public interface ConsumeWsTicketUseCase {
    // Tiêu thụ vé một lần và trả danh tính dùng để tạo Principal cho CONNECT.
    WsTicketIdentity consumeWebSocketTicketAndGetIdentity(String ticket);
}
