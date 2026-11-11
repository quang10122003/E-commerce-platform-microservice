package com.example.auth_service.application.strategy.outbox;

import com.example.auth_service.application.DTO.OutboxEventDto;
import com.example.auth_service.application.DTO.event.StoredFilesDeleteOutboxPayload;
import com.example.auth_service.application.port.out.PayloadMapperPort;
import com.example.auth_service.application.service.DeleteStoredFilesService;
import com.example.auth_service.application.strategy.EventHandler;

// Dọn file trên storage sau khi transaction đổi logo hoặc xóa tài khoản đã commit.
public class StoredFilesDeleteOutboxHandler implements EventHandler<OutboxEventDto> {
    private final PayloadMapperPort payloadMapperPort;
    private final DeleteStoredFilesService deleteStoredFilesService;

    public StoredFilesDeleteOutboxHandler(PayloadMapperPort payloadMapperPort,
                                          DeleteStoredFilesService deleteStoredFilesService) {
        this.payloadMapperPort = payloadMapperPort;
        this.deleteStoredFilesService = deleteStoredFilesService;
    }

    @Override
    public String eventType() {
        return StoredFilesDeleteOutboxPayload.EVENT_TYPE;
    }

    @Override
    public void handle(OutboxEventDto event) {
        StoredFilesDeleteOutboxPayload payload = payloadMapperPort.convert(event.payload(), StoredFilesDeleteOutboxPayload.class);
        deleteStoredFilesService.deleteStoredFiles(payload.files());
    }
}
