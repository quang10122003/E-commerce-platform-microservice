package com.example.notification.application.port.in;

import com.example.notification.application.dto.WsTicketIdentity;

public interface ConsumeWsTicketUseCase {
    WsTicketIdentity consumeWebSocketTicketAndGetIdentity(String ticket);
}
