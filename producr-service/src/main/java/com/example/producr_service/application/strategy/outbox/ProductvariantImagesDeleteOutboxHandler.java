package com.example.producr_service.application.strategy.outbox;

import com.example.producr_service.application.dto.outbox.OutboxEventDto;
import com.example.producr_service.application.dto.outbox.ProductvariantImagesDeleteOutboxPayload;
import com.example.producr_service.application.port.out.Json.MapJsonToObjPort;
import com.example.producr_service.application.service.StoredFileCleaner;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RequiredArgsConstructor
@FieldDefaults(makeFinal = true,level = AccessLevel.PRIVATE)
public class ProductvariantImagesDeleteOutboxHandler implements OutboxEventHandler{
    MapJsonToObjPort mapJsonToObjPort;
    StoredFileCleaner storedFileCleaner;
    @Override
    public String eventType() {
        return ProductvariantImagesDeleteOutboxPayload.EVENT_TYPE;
    }

    @Override
    public void handle(OutboxEventDto event) {
        ProductvariantImagesDeleteOutboxPayload payload  = mapJsonToObjPort.readValue(event.payload(),ProductvariantImagesDeleteOutboxPayload.class);
        storedFileCleaner.deleteStoredFiles(payload.storedFileList());
    }
}
