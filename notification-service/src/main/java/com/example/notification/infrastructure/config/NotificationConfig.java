package com.example.notification.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.notification.application.registry.KafkaEventHandlerRegistry;
import com.example.notification.application.dto.template.UserRegisteredEmailTemplate;
import com.example.notification.application.dto.template.CheckoutSucceededEmailTemplate;
import com.example.notification.application.strategy.EventHandler;
import com.example.notification.application.strategy.event.UserRegisteredEventHandler;
import com.example.notification.application.service.NotificationApplicationService;
import com.example.notification.application.service.AuthEventApplicationService;
import com.example.notification.application.port.in.ProcessAuthEventUseCase;
import com.example.notification.application.port.in.ProcessOrderEventUseCase;
import com.example.notification.application.port.out.NotificationStreamPort;
import com.example.notification.application.port.out.client.CheckoutAccessPort;
import com.example.notification.application.service.CheckoutNotificationApplicationService;
import com.example.notification.application.service.OrderEventApplicationService;
import com.example.notification.application.strategy.event.CheckoutSucceededEventHandler;
import com.example.notification.application.strategy.event.CheckoutFailedEventHandler;
import com.example.notification.application.port.out.mail.EmailSenderPort;
import com.example.notification.application.port.out.objectMapper.ObjectMapperPort;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.time.Clock;

@Configuration
@Slf4j
public class NotificationConfig {

    @Bean
    NotificationApplicationService sendEmailUserRegisteredUseCase(
            EmailSenderPort emailSenderPort,
            UserRegisteredEmailTemplate template,
            CheckoutSucceededEmailTemplate checkoutTemplate
    ) {
        return new NotificationApplicationService(emailSenderPort, template, checkoutTemplate);
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
    ProcessOrderEventUseCase processOrderEventUseCase(KafkaEventHandlerRegistry eventHandlerRegistry) {
        return new OrderEventApplicationService(eventHandlerRegistry);
    }

    @Bean
    CheckoutSucceededEventHandler checkoutSucceededEventHandler(
            ObjectMapperPort objectMapperPort, CheckoutNotificationApplicationService service,
            NotificationApplicationService emailService) {
        return new CheckoutSucceededEventHandler(objectMapperPort, service, emailService);
    }

    @Bean
    CheckoutFailedEventHandler checkoutFailedEventHandler(
            ObjectMapperPort objectMapperPort, CheckoutNotificationApplicationService service) {
        return new CheckoutFailedEventHandler(objectMapperPort, service);
    }

    @Bean
    CheckoutNotificationApplicationService checkoutNotificationApplicationService(
            CheckoutAccessPort checkoutAccessPort, NotificationStreamPort notificationStreamPort) {
        return new CheckoutNotificationApplicationService(checkoutAccessPort, notificationStreamPort);
    }

    @Bean
    UserRegisteredEmailTemplate userRegisteredEmailTemplate(MailSenderProperties mailSenderProperties) {
        return new UserRegisteredEmailTemplate(mailSenderProperties.welcomeSender());
    }

    @Bean
    CheckoutSucceededEmailTemplate checkoutSucceededEmailTemplate(MailSenderProperties mailSenderProperties, Clock clock) {
        return new CheckoutSucceededEmailTemplate(mailSenderProperties.orderSender(), clock.getZone());
    }

    @Bean
    KafkaEventHandlerRegistry kafkaEventHandlerRegistry(
            List<EventHandler<String>> eventHandlers
    ) {
        if (eventHandlers.isEmpty()) {
            throw new IllegalStateException("Chưa đăng ký handler xử lý event Kafka");
        }
        log.info("Khởi tạo registry event Kafka: event_types={}",
                eventHandlers.stream().map(EventHandler<String>::eventType).toList());
        return new KafkaEventHandlerRegistry(eventHandlers);
    }
}
