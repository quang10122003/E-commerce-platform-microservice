package com.example.notification.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.notification.application.port.out.EmailSenderPort;
import com.example.notification.application.service.NotificationApplicationService;
import com.example.notification.application.dto.template.UserRegisteredEmailTemplate;

@Configuration
public class NotificationConfig {

    // Tạo application service với các dependency qua port và template.
    @Bean
    NotificationApplicationService notificationApplicationService(
            EmailSenderPort emailSenderPort,
            UserRegisteredEmailTemplate userRegisteredEmailTemplate) {
        return new NotificationApplicationService(
                emailSenderPort,
                userRegisteredEmailTemplate);
    }
}
