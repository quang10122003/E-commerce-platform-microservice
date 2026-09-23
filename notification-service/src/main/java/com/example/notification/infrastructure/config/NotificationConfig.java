package com.example.notification.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.notification.application.port.in.SendEmailUserRegisteredUseCase;
import com.example.notification.application.port.out.EmailSenderPort;
import com.example.notification.application.port.out.UserRegisteredEventParserPort;
import com.example.notification.application.registry.AuthEventHandlerRegistry;
import com.example.notification.application.service.NotificationApplicationService;
import com.example.notification.application.dto.template.UserRegisteredEmailTemplate;
import com.example.notification.application.strategy.event.AuthEventHandler;
import com.example.notification.application.strategy.event.UserRegisteredEventHandler;

import java.util.List;

@Configuration
public class NotificationConfig {

    // Khởi tạo template email tại tầng infrastructure để application không phụ thuộc Spring.
    @Bean
    UserRegisteredEmailTemplate userRegisteredEmailTemplate(MailSenderProperties mailSenderProperties) {
        return new UserRegisteredEmailTemplate(mailSenderProperties.welcomeSender());
    }

    // Tạo application service với các dependency qua port và template.
    @Bean
    SendEmailUserRegisteredUseCase notificationApplicationService(
            EmailSenderPort emailSenderPort,
            UserRegisteredEmailTemplate userRegisteredEmailTemplate) {
        return new NotificationApplicationService(
                emailSenderPort,
                userRegisteredEmailTemplate);
    }

    // Đăng ký strategy xử lý event đăng ký người dùng qua các port application.
    @Bean
    AuthEventHandler userRegisteredEventHandler(
            SendEmailUserRegisteredUseCase emailUserRegisteredUseCase,
            UserRegisteredEventParserPort eventParserPort
    ) {
        return new UserRegisteredEventHandler(
                emailUserRegisteredUseCase,
                eventParserPort
        );
    }

    // Gom các strategy event auth để định tuyến theo eventType.
    @Bean
    AuthEventHandlerRegistry authEventHandlerRegistry(
            List<AuthEventHandler> eventHandlers
    ) {
        return new AuthEventHandlerRegistry(eventHandlers);
    }
}
