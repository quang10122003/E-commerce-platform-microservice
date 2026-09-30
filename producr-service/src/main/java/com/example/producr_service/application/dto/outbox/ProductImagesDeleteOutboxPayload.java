package com.example.producr_service.application.dto.outbox;

import com.example.producr_service.application.port.out.storage.StoredFile;

import java.util.List;

public record ProductImagesDeleteOutboxPayload(List<StoredFile> files)  {
    public static final String EVENT_TYPE = "PRODUCT_IMAGES_DELETE";
}
