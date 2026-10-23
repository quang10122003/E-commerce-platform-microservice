package com.example.producr_service.application.strategy.eventKafka;

import com.example.producr_service.application.dto.event.CartItemAddedEvent;
import com.example.producr_service.application.port.out.Json.MapJsonToObjPort;
import com.example.producr_service.application.service.ProductRecommendationService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CartItemAddedEventHandler implements EventHandler {
    private final MapJsonToObjPort mapJsonToObjPort;
    private final ProductRecommendationService productRecommendationService;

    @Override
    public String eventType() {
        return CartItemAddedEvent.EVENT_TYPE;
    }

    @Override
    public void handle(String message) {
        CartItemAddedEvent event = mapJsonToObjPort.readValue(message, CartItemAddedEvent.class);
        productRecommendationService.recordAddToCart(event);
    }
}
