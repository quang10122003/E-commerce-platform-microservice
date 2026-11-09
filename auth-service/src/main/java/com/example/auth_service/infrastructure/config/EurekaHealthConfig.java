package com.example.auth_service.infrastructure.config;

import com.example.common.discovery.HealthGroupEurekaHealthCheckHandler;
import com.netflix.appinfo.HealthCheckHandler;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.health.actuate.endpoint.HealthEndpoint;
import org.springframework.boot.health.actuate.endpoint.HealthEndpointGroups;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnBooleanProperty("eureka.client.healthcheck.enabled")
public class EurekaHealthConfig {

    // Thay handler mặc định để Eureka chỉ xét health group eureka thay vì mọi health indicator.
    @Bean
    HealthCheckHandler healthGroupEurekaHealthCheckHandler(HealthEndpoint healthEndpoint,
                                                           HealthEndpointGroups healthEndpointGroups) {
        return new HealthGroupEurekaHealthCheckHandler(healthEndpoint, healthEndpointGroups, "eureka");
    }
}
