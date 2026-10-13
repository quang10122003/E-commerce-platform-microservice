package com.example.notification.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.notification.application.registry.AuthEventHandlerRegistry;
import com.example.notification.application.dto.template.UserRegisteredEmailTemplate;
import com.example.notification.application.strategy.event.AuthEventHandler;

import java.util.List;

@Configuration
public class NotificationConfig {

    // Khởi tạo template email tại tầng infrastructure để application không phụ thuộc Spring.
    @Bean
    UserRegisteredEmailTemplate userRegisteredEmailTemplate(MailSenderProperties mailSenderProperties) {
        return new UserRegisteredEmailTemplate(mailSenderProperties.welcomeSender());
    }

    // Gom các strategy event auth để định tuyến theo eventType.
    @Bean
    AuthEventHandlerRegistry authEventHandlerRegistry(
            List<AuthEventHandler> eventHandlers
    ) {
        return new AuthEventHandlerRegistry(eventHandlers);
    }
}
