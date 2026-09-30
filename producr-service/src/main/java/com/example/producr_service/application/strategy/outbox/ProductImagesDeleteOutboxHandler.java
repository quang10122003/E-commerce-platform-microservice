package com.example.producr_service.application.strategy.outbox;

import com.example.producr_service.application.dto.outbox.OutboxEventDto;
import com.example.producr_service.application.dto.outbox.ProductImagesDeleteOutboxPayload;
import com.example.producr_service.application.port.out.Json.MapJsonToObjPort;
import com.example.producr_service.application.service.StoredFileCleaner;

public class ProductImagesDeleteOutboxHandler implements OutboxEventHandler {
    private final MapJsonToObjPort mapJsonToObjPort;
    private final StoredFileCleaner storedFileCleaner;

    public ProductImagesDeleteOutboxHandler(MapJsonToObjPort mapJsonToObjPort, StoredFileCleaner storedFileCleaner) {
        this.mapJsonToObjPort = mapJsonToObjPort;
        this.storedFileCleaner = storedFileCleaner;
    }

    @Override
    public String eventType() {
        return ProductImagesDeleteOutboxPayload.EVENT_TYPE;
    }

    @Override
    public void handle(OutboxEventDto event) {
        ProductImagesDeleteOutboxPayload payload = mapJsonToObjPort.readValue(
                event.payload(), ProductImagesDeleteOutboxPayload.class);
        storedFileCleaner.deleteStoredFiles(payload.files());
    }
}
