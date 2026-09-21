package com.example.producr_service.adapter.out.event;

import com.example.producr_service.application.dto.event.ProductCreatedEvent;
import com.example.producr_service.application.port.out.ProductCreatedEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SpringProductCreatedEventPublisher implements ProductCreatedEventPublisher {

    // Dùng cơ chế event của Spring để phát ProductCreatedEvent.
    private final ApplicationEventPublisher applicationEventPublisher;

    // Chuyển event của application sang Spring event.
    @Override
    public void publish(ProductCreatedEvent event) {
        applicationEventPublisher.publishEvent(event);
    }
}
