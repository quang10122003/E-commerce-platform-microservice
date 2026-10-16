package com.example.notification.infrastructure.config;

import com.example.notification.application.port.out.WsTicketStorePort;
import com.example.notification.application.service.WsTicketApplicationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;

@Configuration
public class WsTicketConfig {
    // Dùng SecureRandom của Java để sinh vé, không dùng JWT làm vé kết nối.
    @Bean
    SecureRandom wsTicketSecureRandom() {
        return new SecureRandom();
    }

    // Lắp ghép hai input port cấp/tiêu thụ vé qua cùng output port lưu trữ.
    @Bean
    WsTicketApplicationService wsTicketApplicationService(WsTicketStorePort wsTicketStorePort,
                                                        SecureRandom wsTicketSecureRandom, Clock chatClock,
                                                        @Value("${app.websocket.ticket-ttl-seconds:30}") long ticketTtlSeconds) {
        if (ticketTtlSeconds <= 0) {
            throw new IllegalArgumentException("TTL vé WebSocket phải lớn hơn 0");
        }
        return new WsTicketApplicationService(wsTicketStorePort, wsTicketSecureRandom, chatClock,
                Duration.ofSeconds(ticketTtlSeconds));
    }
}
