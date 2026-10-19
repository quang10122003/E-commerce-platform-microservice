package com.example.notification.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
@ConfigurationProperties(prefix = "app.mail")
public record MailSenderProperties(
        String welcomeSender
       ) {
}