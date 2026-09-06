package com.example.notification.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
// lấy config domain để gửi mall 
@ConfigurationProperties(prefix = "app.mail")
public record MailSenderProperties(
        String welcomeSender
       ) {
}