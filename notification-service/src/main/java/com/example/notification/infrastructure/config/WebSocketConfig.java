package com.example.notification.infrastructure.config;
import com.example.notification.adapter.in.websocket.WebSocketJwtInterceptor;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig
        implements WebSocketMessageBrokerConfigurer {
    private final String[] ALLOWED_ORIGIN = {"http://localhost:3000"};
    private final WebSocketJwtInterceptor webSocketJwtInterceptor;

    public WebSocketConfig(WebSocketJwtInterceptor webSocketJwtInterceptor) {
        this.webSocketJwtInterceptor = webSocketJwtInterceptor;
    }

    // Xác thực JWT trên CONNECT và bảo vệ các frame STOMP inbound.
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(webSocketJwtInterceptor);
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-notifications")
                .setAllowedOriginPatterns(ALLOWED_ORIGIN);
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Nơi server gửi thông báo đến client
        registry.enableSimpleBroker("/topic", "/queue");

        // Prefix cho message client gửi lên server
        registry.setApplicationDestinationPrefixes("/app");
        
    }
}
