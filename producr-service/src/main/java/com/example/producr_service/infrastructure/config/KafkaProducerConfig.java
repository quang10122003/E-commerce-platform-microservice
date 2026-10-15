package com.example.producr_service.infrastructure.config;

import com.example.producr_service.application.constant.Constant;
import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

@Configuration
public class KafkaProducerConfig {

    // Tạo producer gửi payload đã serialize thành String theo convention của Auth Service.
    @Bean
    ProducerFactory<String, String> producerFactory(
            @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers,
            @Value("${spring.kafka.producer.acks:all}") String acks,
            @Value("${spring.kafka.producer.properties.enable.idempotence:true}") boolean enableIdempotence,
            @Value("${spring.kafka.producer.properties.delivery.timeout.ms:120000}") int deliveryTimeoutMs,
            @Value("${spring.kafka.producer.properties.linger.ms:5}") int lingerMs) {
        Map<String, Object> properties = new HashMap<>();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        properties.put(ProducerConfig.ACKS_CONFIG, acks);
        properties.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, enableIdempotence);
        properties.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, deliveryTimeoutMs);
        properties.put(ProducerConfig.LINGER_MS_CONFIG, lingerMs);
        return new DefaultKafkaProducerFactory<>(properties);
    }

    // Cung cấp KafkaTemplate dùng chung cho các publisher được bổ sung sau.
    @Bean
    KafkaTemplate<String, String> kafkaTemplate(ProducerFactory<String, String> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    // Khai báo topic event product với số partition và replica giống Auth Service.
    @Bean
    NewTopic productEventsTopic() {
        return TopicBuilder.name(Constant.PRODUCT_EVENTS_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
