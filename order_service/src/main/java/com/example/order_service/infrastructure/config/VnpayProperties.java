package com.example.order_service.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.vnpay")
public record VnpayProperties(String paymentUrl, String tmnCode, String hashSecret,
                              String returnUrl, String version, String command,
                              String currency, String locale, String orderType,
                              int timeoutMinutes, String queryUrl, int reconcileGraceMinutes) {
}
