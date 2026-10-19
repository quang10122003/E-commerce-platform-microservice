package com.example.notification.infrastructure.config;
import com.example.notification.adapter.in.websocket.WebSocketJwtInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig
        implements WebSocketMessageBrokerConfigurer {
    private final String[] allowedOrigins;
    private final WebSocketJwtInterceptor webSocketJwtInterceptor;

    public WebSocketConfig(WebSocketJwtInterceptor webSocketJwtInterceptor,
                           @Value("${app.websocket.allowed-origins:http://localhost:3000}") String[] allowedOrigins) {
        this.webSocketJwtInterceptor = webSocketJwtInterceptor;
        this.allowedOrigins = allowedOrigins;
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(webSocketJwtInterceptor);
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-notifications")
                .setAllowedOriginPatterns(allowedOrigins);
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // gửi tin nhắn từ server về clent đăng ký
        registry.enableSimpleBroker("/topic", "/queue");

        // clinet gửi server
        registry.setApplicationDestinationPrefixes("/app");
//        tiền tố dùng để gửi tin nhắn đến người dùng cụ thể
        registry.setUserDestinationPrefix("/user");
        
    }
}
