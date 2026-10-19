package com.example.notification.application.port.in;

import com.example.notification.application.dto.WsTicketIdentity;
import com.example.notification.application.dto.response.WsTicketResponse;

public interface IssueWsTicketUseCase {
    WsTicketResponse issueWebSocketConnectionTicket(WsTicketIdentity identity);
}
