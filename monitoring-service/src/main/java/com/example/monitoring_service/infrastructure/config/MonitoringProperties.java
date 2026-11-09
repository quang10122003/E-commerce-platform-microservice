package com.example.monitoring_service.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.List;

@ConfigurationProperties(prefix = "app.monitoring")
public record MonitoringProperties(Duration requestTimeout, List<String> expectedServices) {

    public MonitoringProperties {
        expectedServices = expectedServices == null ? List.of() : List.copyOf(expectedServices);
    }
}
