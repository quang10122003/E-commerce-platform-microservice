package com.example.notification.infrastructure.config;

import com.example.notification.application.constant.Constant;
import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.listener.ContainerPausingBackOffHandler;
import org.springframework.kafka.listener.ListenerContainerPauseService;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.util.backoff.BackOff;
import org.springframework.util.backoff.BackOffExecution;

@EnableKafka
@EnableScheduling
@Configuration
public class KafkaConsumerConfig {
    @Bean
    ConsumerFactory<String, String> consumerFactory(
            @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers,
            @Value("${spring.kafka.consumer.group-id}") String groupId,
            @Value("${spring.kafka.consumer.auto-offset-reset}") String autoOffsetReset) {
        Map<String, Object> properties = new HashMap<>();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetReset);
        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        return new DefaultKafkaConsumerFactory<>(properties);
    }

    // Cấu hình số lần retry của một event.
    @Bean
    BackOff kafkaEventBackOff(
            @Value("${app.kafka.consumer.retry-interval-ms}") long retryIntervalMs,
            @Value("${app.kafka.consumer.retry-increment-ms}") long retryIncrementMs,
            @Value("${app.kafka.consumer.max-retries}") long maxRetries) {
        if (retryIntervalMs < 0 || retryIncrementMs < 0 || maxRetries < 0) {
            throw new IllegalArgumentException("Khoảng chờ và số lần retry Kafka không được âm");
        }
        if (maxRetries > 0) {
            Math.addExact(retryIntervalMs, Math.multiplyExact(maxRetries - 1, retryIncrementMs));
        }
        return () -> new BackOffExecution() {
            private long retries;

            @Override
            public long nextBackOff() {
                if (retries >= maxRetries) {
                    return STOP;
                }
                return retryIntervalMs + retries++ * retryIncrementMs;
            }
        };
    }

    // Cấu hình khi consumer ném lỗi.
    @Bean
    DefaultErrorHandler kafkaEventErrorHandler(
            KafkaTemplate<String, String> kafkaTemplate, BackOff kafkaEventBackOff,
            @Qualifier("taskScheduler") TaskScheduler taskScheduler) {
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(kafkaTemplate,
                (record, exception) -> new TopicPartition(Constant.DEAD_LETTER_TOPIC, record.partition()));
        // Chỉ bỏ qua record sau khi broker xác nhận lưu DLT, tránh mất event khi broker gặp lỗi.
        recoverer.setFailIfSendResultIsError(true);
        // Pause thay vì sleep dài để consumer vẫn poll, tránh vượt max.poll.interval.ms khi chờ retry.
        ContainerPausingBackOffHandler backOffHandler = new ContainerPausingBackOffHandler(
                new ListenerContainerPauseService(null, taskScheduler));
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(recoverer, kafkaEventBackOff, backOffHandler);
        errorHandler.setResetStateOnExceptionChange(false);
        return errorHandler;
    }

    @Bean
    ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory(
            ConsumerFactory<String, String> consumerFactory, DefaultErrorHandler kafkaEventErrorHandler) {
        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.setCommonErrorHandler(kafkaEventErrorHandler);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.BATCH);
        return factory;
    }

    @Bean
    NewTopic deadLetterTopic(
            @Value("${app.kafka.consumer.dlt-partitions}") int partitions,
            @Value("${app.kafka.consumer.dlt-replicas}") int replicas) {
        return TopicBuilder.name(Constant.DEAD_LETTER_TOPIC)
                .partitions(partitions).replicas(replicas).build();
    }
}
