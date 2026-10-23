package com.example.order_service.cart.adapter.out.kafka;

import com.example.order_service.cart.application.port.out.objettMapper.ObjectMapperPort;
import com.example.order_service.cart.application.Constant.Constant;
import com.example.order_service.cart.application.dto.outbox.OutboxEventDto;
import com.example.order_service.cart.application.port.out.kafka.OrderEventPublisherPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaOrderEventProducer implements OrderEventPublisherPort {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapperPort objectMapperPort;

    @Override
    public void publish(OutboxEventDto event) {
        try {
            kafkaTemplate.send(Constant.ORDER_EVENTS_TOPIC, event.eventId().toString(), objectMapperPort.writeValue(event.payload()))
                    .get(Constant.OUTBOX_PUBLISH_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            log.info("Phát event thành công: topic={}, event_id={}, event_type={}",
                    Constant.ORDER_EVENTS_TOPIC, event.eventId(), event.eventType());
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Bị gián đoạn khi phát event order", exception);
        } catch (Exception exception) {
            throw new IllegalStateException("Không thể phát event order", exception);
        }
    }
}
