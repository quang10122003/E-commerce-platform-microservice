package com.example.notification.infrastructure.config;

import com.example.notification.application.port.out.repo.BannerRepositoryPort;
import com.example.notification.application.service.BannerApplicationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BannerConfig {

    @Bean
    BannerApplicationService createBannerUseCase(BannerRepositoryPort bannerRepositoryPort) {
        return new BannerApplicationService(bannerRepositoryPort);
    }
}
