package com.example.notification.adapter.in.websocket;

import com.example.common.error.AuthorizationError;
import com.example.common.exception.BusinessException;
import com.example.common.security.AuthorizationUtils;
import com.example.common.security.JwtVerifier;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class WebSocketJwtInterceptor implements ChannelInterceptor {

    private static final String TOKEN_EXPIRES_AT = "chatTokenExpiresAt";
    private static final Set<String> CHAT_SUBSCRIPTIONS = Set.of(
            "/user/queue/chat/messages", "/user/queue/chat/errors");
    private final JwtVerifier jwtVerifier;

    // Xác thực CONNECT và bảo vệ các frame đọc/ghi dữ liệu sau khi kết nối.
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null || accessor.getCommand() == null) {
            return message;
        }
        if (StompCommand.CONNECT.equals(accessor.getCommand()) || StompCommand.STOMP.equals(accessor.getCommand())) {
            authenticate(accessor);
        } else if (StompCommand.SEND.equals(accessor.getCommand())
                || StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            requireAuthenticated(accessor);
            String destination = accessor.getDestination();
            // Client chỉ gửi qua application, không được tự phát tin vào broker của người khác.
            if (StompCommand.SEND.equals(accessor.getCommand())
                    && (destination == null || !destination.startsWith("/app/"))) {
                throw new BusinessException(AuthorizationError.ACCESS_DENIED);
            }
            // Chỉ mở queue chat của chính session; giữ các topic thông báo hiện có.
            if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())
                    && (destination == null || (!CHAT_SUBSCRIPTIONS.contains(destination)
                    && (!destination.startsWith("/topic/") || destination.contains("/chat/"))))) {
                throw new BusinessException(AuthorizationError.ACCESS_DENIED);
            }
        }
        return message;
    }

    // Dùng bộ xác minh JWT , chỉ lưu hạn token và userId trong session.
    private void authenticate(StompHeaderAccessor accessor) {
        String token = AuthorizationUtils.extractBearerToken(accessor.getFirstNativeHeader("Authorization"));
        try {
            var claims = jwtVerifier.extractAllClaims(token);
            Long userId = claims.get("userId", Long.class);
            if (!"access".equals(claims.get("tokenType", String.class)) || userId == null || userId <= 0
                    || claims.getExpiration() == null || accessor.getSessionAttributes() == null) {
                throw new BusinessException(AuthorizationError.UNAUTHENTICATED);
            }
            List<?> roles = claims.get("roles", List.class);
            var authorities = roles == null ? List.<SimpleGrantedAuthority>of()
                    : roles.stream().map(role -> new SimpleGrantedAuthority((String) role)).toList();
            accessor.setUser(UsernamePasswordAuthenticationToken.authenticated(userId.toString(), null, authorities));
            accessor.getSessionAttributes().put(TOKEN_EXPIRES_AT, claims.getExpiration().toInstant());
        } catch (JwtException | IllegalArgumentException | ClassCastException exception) {
            throw new BusinessException(AuthorizationError.UNAUTHENTICATED);
        }
    }

    // Frame SEND/SUBSCRIBE chỉ dùng danh tính của CONNECT và token còn hạn.
    private void requireAuthenticated(StompHeaderAccessor accessor) {
        if (!(accessor.getUser() instanceof Authentication authentication) || !authentication.isAuthenticated()
                || accessor.getSessionAttributes() == null
                || !(accessor.getSessionAttributes().get(TOKEN_EXPIRES_AT) instanceof Instant expiresAt)
                || !expiresAt.isAfter(Instant.now())) {
            throw new BusinessException(AuthorizationError.UNAUTHENTICATED);
        }
    }
}
