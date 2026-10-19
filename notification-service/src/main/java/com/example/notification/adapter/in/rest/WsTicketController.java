package com.example.notification.adapter.in.rest;

import io.jsonwebtoken.Claims;
import com.example.common.error.AuthorizationError;
import com.example.common.exception.BusinessException;
import com.example.common.security.AuthorizationUtils;
import com.example.common.security.JwtVerifier;
import com.example.notification.application.dto.WsTicketIdentity;
import com.example.notification.application.dto.response.WsTicketResponse;
import com.example.notification.application.port.in.IssueWsTicketUseCase;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class WsTicketController {
    private final IssueWsTicketUseCase issueWsTicketUseCase;
    private final JwtVerifier jwtVerifier;

    @PostMapping("/ws-ticket")
    public ResponseEntity<WsTicketResponse> issueWebSocketConnectionTicket(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization) {
        WsTicketIdentity identity = extractIdentityFromAuthorizationHeader(authorization);
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
                .header(HttpHeaders.PRAGMA, "no-cache")
                .body(issueWsTicketUseCase.issueWebSocketConnectionTicket(identity));
    }

    private WsTicketIdentity extractIdentityFromAuthorizationHeader(String authorization) {
        String token = AuthorizationUtils.extractBearerToken(authorization);
        try {
            Claims claims = jwtVerifier.extractAllClaims(token);
            Long userId = claims.get("userId", Long.class);
            if (!"access".equals(claims.get("tokenType", String.class)) || userId == null || userId <= 0
                    || claims.getExpiration() == null) {
                throw new BusinessException(AuthorizationError.UNAUTHENTICATED);
            }
            List<?> roles = claims.get("roles", List.class);
            List<String> authorities = roles == null ? List.<String>of() : roles.stream().map(role -> (String) role).toList();
            return new WsTicketIdentity(userId, authorities, claims.getExpiration().toInstant());
        } catch (JwtException | IllegalArgumentException | ClassCastException | NullPointerException exception) {
            throw new BusinessException(AuthorizationError.UNAUTHENTICATED);
        }
    }
}
