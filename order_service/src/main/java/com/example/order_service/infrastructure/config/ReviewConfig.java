package com.example.order_service.infrastructure.config;

import com.example.order_service.application.dto.outbox.OutboxEventDto;
import com.example.order_service.application.port.out.client.CurrentUserPort;
import com.example.order_service.application.port.out.client.UserAvatarPort;
import com.example.order_service.application.port.out.kafka.OrderEventPublisherPort;
import com.example.order_service.application.port.out.outbox.OutboxPort;
import com.example.order_service.application.port.out.repo.ProductReviewRepoPort;
import com.example.order_service.application.port.out.storage.FileStoragePort;
import com.example.order_service.application.port.out.storage.StoredFileRollbackPort;
import com.example.order_service.application.service.DeleteStoredFilesService;
import com.example.order_service.application.service.ProductReviewService;
import com.example.order_service.application.strategy.EventHandler;
import com.example.order_service.application.strategy.outbox.ProductReviewedOutboxHandler;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.time.Clock;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
public class ReviewConfig {
    // Pool riêng để truy vấn điểm đánh giá không tranh luồng với tác vụ khác; hàng đợi đầy thì chạy ngay trên luồng request.
    @Bean
    ThreadPoolTaskExecutor ratingSummaryExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("rating-summary-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        return executor;
    }

    @Bean
    ProductReviewService productReviewService(CurrentUserPort currentUserPort, UserAvatarPort userAvatarPort,
                                              ProductReviewRepoPort productReviewRepoPort,
                                              FileStoragePort fileStoragePort,
                                              StoredFileRollbackPort storedFileRollbackPort, OutboxPort outboxPort,
                                              @Qualifier("ratingSummaryExecutor") Executor ratingSummaryExecutor,
                                              Clock clock) {
        return new ProductReviewService(currentUserPort, userAvatarPort, productReviewRepoPort, fileStoragePort,
                storedFileRollbackPort, outboxPort, ratingSummaryExecutor, clock);
    }

    @Bean
    DeleteStoredFilesService deleteStoredFilesService(FileStoragePort fileStoragePort) {
        return new DeleteStoredFilesService(fileStoragePort);
    }

    @Bean
    EventHandler<OutboxEventDto> productReviewedOutboxHandler(OrderEventPublisherPort publisher) {
        return new ProductReviewedOutboxHandler(publisher);
    }
}
