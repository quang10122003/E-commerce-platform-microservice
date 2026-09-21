package com.example.producr_service.application.dto.event;

import com.example.producr_service.domain.model.Product;

public record ProductCreatedEvent(Product product) {
}
