package com.example.notification.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.notification.application.registry.KafkaEventHandlerRegistry;
import com.example.notification.application.dto.template.UserRegisteredEmailTemplate;
import com.example.notification.application.strategy.event.EventHandler;
import com.example.notification.application.strategy.event.UserRegisteredEventHandler;
import com.example.notification.application.service.NotificationApplicationService;
import com.example.notification.application.service.AuthEventApplicationService;
import com.example.notification.application.port.in.ProcessAuthEventUseCase;
import com.example.notification.application.port.out.mail.EmailSenderPort;
import com.example.notification.application.port.out.objectMapper.UserRegisteredEventParserPort;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Configuration
@Slf4j
public class NotificationConfig {

    // Lắp ghép use case gửi email qua port và template hiện có.
    @Bean
    NotificationApplicationService sendEmailUserRegisteredUseCase(
            EmailSenderPort emailSenderPort,
            UserRegisteredEmailTemplate template
    ) {
        return new NotificationApplicationService(emailSenderPort, template);
    }

    // Đăng ký strategy xử lý event đăng ký người dùng.
    @Bean
    UserRegisteredEventHandler userRegisteredEventHandler(
            NotificationApplicationService notificationApplicationService,
            UserRegisteredEventParserPort parserPort
    ) {
        return new UserRegisteredEventHandler(notificationApplicationService, parserPort);
    }

    // Công khai boundary xử lý event auth cho consumer, không lộ registry ra adapter.
    @Bean
    ProcessAuthEventUseCase processAuthEventUseCase(KafkaEventHandlerRegistry eventHandlerRegistry) {
        return new AuthEventApplicationService(eventHandlerRegistry);
    }

    // Khởi tạo template email tại tầng infrastructure để application không phụ thuộc Spring.
    @Bean
    UserRegisteredEmailTemplate userRegisteredEmailTemplate(MailSenderProperties mailSenderProperties) {
        return new UserRegisteredEmailTemplate(mailSenderProperties.welcomeSender());
    }

    // Gom các strategy event auth để định tuyến theo eventType.
    @Bean
    KafkaEventHandlerRegistry authEventHandlerRegistry(
            List<EventHandler> eventHandlers
    ) {
        if (eventHandlers.isEmpty()) {
            throw new IllegalStateException("Chưa đăng ký handler xử lý event auth");
        }
        log.info("Khởi tạo registry event auth: event_types={}",
                eventHandlers.stream().map(EventHandler::eventType).toList());
        return new KafkaEventHandlerRegistry(eventHandlers);
    }
}
