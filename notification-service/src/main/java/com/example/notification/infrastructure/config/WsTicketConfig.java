package com.example.notification.infrastructure.config;

import com.example.notification.application.port.out.cache.CachePort;
import com.example.notification.application.service.WsTicketApplicationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;

@Configuration
public class WsTicketConfig {
    // Vé kết nối dùng giá trị ngẫu nhiên thay JWT để không đưa JWT vào vé CONNECT.
    @Bean
    SecureRandom wsTicketSecureRandom() {
        return new SecureRandom();
    }

    @Bean
    WsTicketApplicationService wsTicketApplicationService(CachePort cachePort,
                                                        SecureRandom wsTicketSecureRandom, Clock clock,
                                                        @Value("${app.websocket.ticket-ttl-seconds:30}") long ticketTtlSeconds) {
        if (ticketTtlSeconds <= 0) {
            throw new IllegalArgumentException("TTL vé WebSocket phải lớn hơn 0");
        }
        return new WsTicketApplicationService(cachePort, wsTicketSecureRandom, clock,
                Duration.ofSeconds(ticketTtlSeconds));
    }
}
