package com.example.producr_service.application.strategy.outbox;

import com.example.producr_service.application.strategy.EventHandler;
import com.example.producr_service.application.dto.outbox.OutboxEventDto;
import com.example.producr_service.application.dto.outbox.StoredFilesDeleteOutboxPayload;
import com.example.producr_service.application.port.out.Json.MapJsonToObjPort;
import com.example.producr_service.application.service.StoredFileCleaner;

public class StoredFilesDeleteOutboxHandler implements EventHandler<OutboxEventDto> {
    private final MapJsonToObjPort mapJsonToObjPort;
    private final StoredFileCleaner storedFileCleaner;

    public StoredFilesDeleteOutboxHandler(MapJsonToObjPort mapJsonToObjPort, StoredFileCleaner storedFileCleaner) {
        this.mapJsonToObjPort = mapJsonToObjPort;
        this.storedFileCleaner = storedFileCleaner;
    }

    @Override
    public String eventType() {
        return StoredFilesDeleteOutboxPayload.EVENT_TYPE;
    }

    @Override
    public void handle(OutboxEventDto event) {
        StoredFilesDeleteOutboxPayload payload = mapJsonToObjPort.readValue(
                event.payload(), StoredFilesDeleteOutboxPayload.class);
        storedFileCleaner.deleteStoredFiles(payload.files());
    }
}
