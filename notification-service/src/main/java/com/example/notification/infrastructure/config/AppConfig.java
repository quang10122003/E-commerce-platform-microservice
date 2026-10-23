package com.example.notification.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.task.ThreadPoolTaskSchedulerBuilder;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.time.Clock;
import java.time.ZoneId;

@Configuration
public class AppConfig {
    // WebSocket có scheduler riêng nên Boot không tự tạo bean taskScheduler cho ứng dụng.
    @Bean(name = "taskScheduler")
    ThreadPoolTaskScheduler taskScheduler(ThreadPoolTaskSchedulerBuilder builder, Clock clock) {
        ThreadPoolTaskScheduler scheduler = builder.build();
        scheduler.setClock(clock);
        return scheduler;
    }

    // Hạn vé, hạn phiên và offset response phải dùng cùng múi giờ cấu hình.
    @Bean
    Clock clock() {
        return Clock.system(ZoneId.of("Asia/Ho_Chi_Minh"));
    }
}
