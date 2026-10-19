package com.example.notification.application.service;

import java.time.Instant;
import com.example.common.error.AuthorizationError;
import com.example.common.exception.BusinessException;
import com.example.notification.application.dto.WsTicketData;
import com.example.notification.application.dto.WsTicketIdentity;
import com.example.notification.application.dto.response.WsTicketResponse;
import com.example.notification.application.port.in.ConsumeWsTicketUseCase;
import com.example.notification.application.port.in.IssueWsTicketUseCase;
import com.example.notification.application.port.out.WsTicketStorePort;
import com.example.notification.application.error.WebSocketError;
import lombok.RequiredArgsConstructor;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.util.Base64;
import java.util.regex.Pattern;

@RequiredArgsConstructor
public class WsTicketApplicationService implements IssueWsTicketUseCase, ConsumeWsTicketUseCase {
    private static final Pattern TICKET_PATTERN = Pattern.compile("[A-Za-z0-9_-]{43}");
    private final WsTicketStorePort wsTicketStorePort;
    private final SecureRandom secureRandom;
    private final Clock clock;
    private final Duration ticketTtl;

    // tạo ticket
    @Override
    public WsTicketResponse issueWebSocketConnectionTicket(WsTicketIdentity identity) {
        validateIdentityAndSocketSession(identity);
        Instant now = clock.instant();

        // Vé không được sống lâu hơn phiên JWT đã cấp nó.
        Instant expiresAt = now.plus(ticketTtl);

        if (identity.sessionExpiresAt().isBefore(expiresAt)) {
            expiresAt = identity.sessionExpiresAt();
        }
        Duration ttl = Duration.between(now, expiresAt);
        if (ttl.toMillis() <= 0) {
            throw new BusinessException(AuthorizationError.UNAUTHENTICATED);
        }

        WsTicketData data = new WsTicketData(identity, expiresAt);
        // Chỉ thử lại khi trùng vé để không ghi đè vé đã cấp; giới hạn ba lần để tránh lặp vô hạn.
        for (int attempt = 0; attempt < 3; attempt++) {
            byte[] randomBytes = new byte[32];
            secureRandom.nextBytes(randomBytes);
            String ticket = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
            if (wsTicketStorePort.saveWsTicketIfAbsent(ticket, data, ttl)) {
                return new WsTicketResponse(ticket, expiresAt.atZone(clock.getZone()).toOffsetDateTime());
            }
        }
        throw new BusinessException(WebSocketError.WS_TICKET_UNAVAILABLE);
    }

    // đổi tick lấy danh tính phien socket
    @Override
    public WsTicketIdentity consumeWebSocketTicketAndGetIdentity(String ticket) {
        if (ticket == null || !TICKET_PATTERN.matcher(ticket).matches()) {
            throw new BusinessException(AuthorizationError.UNAUTHENTICATED);
        }
        WsTicketData data = wsTicketStorePort.getAndDeleteWsTicket(ticket)
                .orElseThrow(() -> new BusinessException(AuthorizationError.UNAUTHENTICATED));
        if (data.expiresAt() == null || !data.expiresAt().isAfter(clock.instant())) {
            throw new BusinessException(AuthorizationError.UNAUTHENTICATED);
        }
        validateIdentityAndSocketSession(data.identity());
        return data.identity();
    }

    private void validateIdentityAndSocketSession(WsTicketIdentity identity) {
        if (identity == null || identity.userId() == null || identity.userId() <= 0
                || identity.sessionExpiresAt() == null || !identity.sessionExpiresAt().isAfter(clock.instant())
                || identity.roles().stream().anyMatch(role -> role == null || role.isBlank())) {
            throw new BusinessException(AuthorizationError.UNAUTHENTICATED);
        }
    }
}
