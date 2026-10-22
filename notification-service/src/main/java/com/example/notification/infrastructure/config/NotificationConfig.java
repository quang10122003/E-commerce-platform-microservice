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
import com.example.notification.application.port.out.objectMapper.ObjectMapperPort;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Configuration
@Slf4j
public class NotificationConfig {

    @Bean
    NotificationApplicationService sendEmailUserRegisteredUseCase(
            EmailSenderPort emailSenderPort,
            UserRegisteredEmailTemplate template
    ) {
        return new NotificationApplicationService(emailSenderPort, template);
    }

    @Bean
    UserRegisteredEventHandler userRegisteredEventHandler(
            NotificationApplicationService notificationApplicationService,
            ObjectMapperPort objectMapperPort
    ) {
        return new UserRegisteredEventHandler(notificationApplicationService, objectMapperPort);
    }

    @Bean
    ProcessAuthEventUseCase processAuthEventUseCase(KafkaEventHandlerRegistry eventHandlerRegistry) {
        return new AuthEventApplicationService(eventHandlerRegistry);
    }

    @Bean
    UserRegisteredEmailTemplate userRegisteredEmailTemplate(MailSenderProperties mailSenderProperties) {
        return new UserRegisteredEmailTemplate(mailSenderProperties.welcomeSender());
    }

    @Bean
    KafkaEventHandlerRegistry kafkaEventHandlerRegistry(
            List<EventHandler> eventHandlers
    ) {
        if (eventHandlers.isEmpty()) {
            throw new IllegalStateException("Chưa đăng ký handler xử lý event Kafka");
        }
        log.info("Khởi tạo registry event Kafka: event_types={}",
                eventHandlers.stream().map(EventHandler::eventType).toList());
        return new KafkaEventHandlerRegistry(eventHandlers);
    }
}
