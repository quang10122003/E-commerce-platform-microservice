package com.example.notification.application.port.in;

import com.example.notification.application.dto.WsTicketIdentity;
import com.example.notification.application.dto.response.WsTicketResponse;

public interface IssueWsTicketUseCase {
    // Cấp vé từ danh tính do adapter HTTP xác thực, không nhận JWT vào application.
    WsTicketResponse issueWebSocketConnectionTicket(WsTicketIdentity identity);
}
