package com.example.producr_service.application.strategy.eventKafka;

import com.example.producr_service.application.dto.event.UserDeletedEvent;
import com.example.producr_service.application.port.out.Json.MapJsonToObjPort;
import com.example.producr_service.application.service.ProductRecommendationService;
import com.example.producr_service.application.strategy.EventHandler;

public class UserDeletedEventHandler implements EventHandler<String> {
    private final MapJsonToObjPort mapJsonToObjPort;
    private final ProductRecommendationService productRecommendationService;

    public UserDeletedEventHandler(MapJsonToObjPort mapJsonToObjPort,
                                   ProductRecommendationService productRecommendationService) {
        this.mapJsonToObjPort = mapJsonToObjPort;
        this.productRecommendationService = productRecommendationService;
    }

    @Override
    public String eventType() {
        return UserDeletedEvent.EVENT_TYPE;
    }

    @Override
    public void handle(String message) {
        UserDeletedEvent event = mapJsonToObjPort.readValue(message, UserDeletedEvent.class);
        if (event == null || event.userId() == null || event.userId() <= 0) {
            throw new IllegalArgumentException("Event xóa tài khoản thiếu userId hợp lệ");
        }
        productRecommendationService.clearDeletedUserData(event.userId());
    }
}
