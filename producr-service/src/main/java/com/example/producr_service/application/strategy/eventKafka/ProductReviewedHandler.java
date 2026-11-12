package com.example.producr_service.application.strategy.eventKafka;

import com.example.producr_service.application.dto.event.ProductReviewedPayload;
import com.example.producr_service.application.port.out.Json.MapJsonToObjPort;
import com.example.producr_service.application.service.ProductReviewEventService;
import com.example.producr_service.application.strategy.EventHandler;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ProductReviewedHandler implements EventHandler<String> {
    private final MapJsonToObjPort json;
    private final ProductReviewEventService service;

    @Override
    public String eventType() {
        return ProductReviewedPayload.EVENT_TYPE;
    }

    @Override
    public void handle(String message) {
        service.applyProductReviewed(json.readValue(message, ProductReviewedPayload.class));
    }
}
