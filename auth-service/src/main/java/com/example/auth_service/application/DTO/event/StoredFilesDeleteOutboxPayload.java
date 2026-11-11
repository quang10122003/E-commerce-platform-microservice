package com.example.auth_service.application.DTO.event;

import com.example.auth_service.application.port.out.storage.StoredFile;

import java.util.List;

public record StoredFilesDeleteOutboxPayload(List<StoredFile> files) {
    public static final String EVENT_TYPE = "STORED_FILES_DELETE";
}
