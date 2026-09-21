package com.example.producr_service.application.port.out;

import com.example.producr_service.application.dto.event.ProductCreatedEvent;

public interface ProductCreatedEventPublisher {
    void publish(ProductCreatedEvent event);
}
