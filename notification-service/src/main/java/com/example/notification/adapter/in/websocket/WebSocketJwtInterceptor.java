package com.example.notification.adapter.in.websocket;

import com.example.notification.application.dto.WsTicketIdentity;
import java.util.List;
import java.util.regex.Matcher;
import com.example.common.error.AuthorizationError;
import com.example.common.exception.BusinessException;
import com.example.common.security.AuthorizationUtils;
import com.example.notification.application.port.in.CheckChatConversationAccessUseCase;
import com.example.notification.application.port.in.ConsumeWsTicketUseCase;
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

import java.time.Clock;
import java.time.Instant;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class WebSocketJwtInterceptor implements ChannelInterceptor {

    private static final String SESSION_EXPIRES_AT = "chatSessionExpiresAt";
    private static final Pattern CHAT_TOPIC = Pattern.compile("/topic/chat/conversations/([1-9][0-9]*)(?:/read)?");
    private final ConsumeWsTicketUseCase consumeWsTicketUseCase;
    private final CheckChatConversationAccessUseCase checkChatConversationAccessUseCase;
    private final Clock clock;

    // Xác thực CONNECT và bảo vệ các frame đọc/ghi dữ liệu sau khi kết nối.
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null || accessor.getCommand() == null) {
            return message;
        }
        if (StompCommand.CONNECT.equals(accessor.getCommand()) || StompCommand.STOMP.equals(accessor.getCommand())) {
            authenticateWithWsTicket(accessor);
        } else if (StompCommand.SEND.equals(accessor.getCommand())
                || StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            requireValidSocketSession(accessor);
            String destination = accessor.getDestination();
            // Client chỉ gửi qua application, không được tự phát tin vào broker của người khác.
            if (StompCommand.SEND.equals(accessor.getCommand())
                    && (destination == null || !destination.startsWith("/app/"))) {
                throw new BusinessException(AuthorizationError.ACCESS_DENIED);
            }
            // Queue chỉ nhận lỗi; topic chat được kiểm tra thành viên trước khi subscribe.
            if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
                requireSubscriptionAllowed(accessor, destination);
            }
        }
        return message;
    }

    // Chặn wildcard để không thể subscribe bao trùm topic của room không có quyền.
    private void requireSubscriptionAllowed(StompHeaderAccessor accessor, String destination) {
        if (destination == null || destination.contains("*") || destination.contains("?")
                || destination.contains("{") || destination.contains("}")) {
            throw new BusinessException(AuthorizationError.ACCESS_DENIED);
        }
        Matcher matcher = CHAT_TOPIC.matcher(destination);
        if (matcher.matches()) {
            try {
                checkChatConversationAccessUseCase.requireConversationParticipant(Long.parseLong(matcher.group(1)),
                        Long.parseLong(accessor.getUser().getName()));
            } catch (NumberFormatException exception) {
                throw new BusinessException(AuthorizationError.ACCESS_DENIED);
            }
            return;
        }
        if (!"/user/queue/chat/errors".equals(destination)
                && (!destination.startsWith("/topic/") || destination.contains("/chat/"))) {
            throw new BusinessException(AuthorizationError.ACCESS_DENIED);
        }
    }

    // Tiêu thụ vé một lần; giữ hạn phiên từ JWT gốc thay vì hạn 30 giây của vé.
    private void authenticateWithWsTicket(StompHeaderAccessor accessor) {
        String authorization = accessor.getFirstNativeHeader("Authorization");
        // Không giữ credential trong message để tránh lộ qua log lỗi của framework.
        accessor.removeNativeHeader("Authorization");
        if (accessor.getSessionAttributes() == null
                || accessor.getSessionAttributes().containsKey(SESSION_EXPIRES_AT)) {
            throw new BusinessException(AuthorizationError.UNAUTHENTICATED);
        }
        String ticket = AuthorizationUtils.extractBearerToken(authorization);
        WsTicketIdentity identity = consumeWsTicketUseCase.consumeWebSocketTicketAndGetIdentity(ticket);
        List<SimpleGrantedAuthority> authorities = identity.roles().stream().map(SimpleGrantedAuthority::new).toList();
        accessor.setUser(UsernamePasswordAuthenticationToken.authenticated(identity.userId().toString(), null, authorities));
        accessor.getSessionAttributes().put(SESSION_EXPIRES_AT, identity.sessionExpiresAt());
    }

    // Frame SEND/SUBSCRIBE dùng danh tính CONNECT và hạn phiên độc lập với TTL của vé.
    private void requireValidSocketSession(StompHeaderAccessor accessor) {
        if (!(accessor.getUser() instanceof Authentication authentication) || !authentication.isAuthenticated()
                || accessor.getSessionAttributes() == null
                || !(accessor.getSessionAttributes().get(SESSION_EXPIRES_AT) instanceof Instant expiresAt)
                || !expiresAt.isAfter(clock.instant())) {
            throw new BusinessException(AuthorizationError.UNAUTHENTICATED);
        }
    }
}
