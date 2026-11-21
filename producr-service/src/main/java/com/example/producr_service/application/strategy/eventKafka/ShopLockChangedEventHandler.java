package com.example.producr_service.application.strategy.eventKafka;

import com.example.producr_service.application.dto.event.ShopLockChangedEvent;
import com.example.producr_service.application.port.out.Json.MapJsonToObjPort;
import com.example.producr_service.application.service.ProductShopLockService;
import com.example.producr_service.application.strategy.EventHandler;

public class ShopLockChangedEventHandler implements EventHandler<String> {
    private final MapJsonToObjPort mapJsonToObjPort;
    private final ProductShopLockService productShopLockService;

    public ShopLockChangedEventHandler(MapJsonToObjPort mapJsonToObjPort,
                                       ProductShopLockService productShopLockService) {
        this.mapJsonToObjPort = mapJsonToObjPort;
        this.productShopLockService = productShopLockService;
    }

    @Override
    public String eventType() {
        return ShopLockChangedEvent.EVENT_TYPE;
    }

    @Override
    public void handle(String message) {
        ShopLockChangedEvent event = mapJsonToObjPort.readValue(message, ShopLockChangedEvent.class);
        if (event == null || event.shopId() == null || event.shopId() <= 0 || event.occurredAt() == null) {
            throw new IllegalArgumentException("Event khóa shop thiếu shopId hoặc occurredAt hợp lệ");
        }
        productShopLockService.syncShopLock(event.shopId(), event.locked(), event.occurredAt());
    }
}
