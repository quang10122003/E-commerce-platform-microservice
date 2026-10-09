package com.example.producr_service.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

import java.time.Clock;
import java.time.ZoneId;

@Configuration
public class AppConfig {
    // Cung cấp nguồn thời gian theo múi giờ Việt Nam.
    @Bean
    Clock clock() {
        return Clock.system(ZoneId.of("Asia/Ho_Chi_Minh"));
    }

    @Bean
    public RestClient restClient() {
        return RestClient.builder().build();
    }
}
