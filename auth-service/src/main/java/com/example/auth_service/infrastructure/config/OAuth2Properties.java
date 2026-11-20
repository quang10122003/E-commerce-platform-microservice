package com.example.auth_service.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.oauth2")
public record OAuth2Properties(String frontendCallbackUrl, Google google) {

    public record Google(String clientId, String clientSecret, String redirectUri) {
    }
}
