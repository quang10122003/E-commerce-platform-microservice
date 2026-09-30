package com.example.producr_service.application.dto.outbox;

import com.example.producr_service.application.port.out.storage.StoredFile;

import java.util.List;

public record ProductvariantImagesDeleteOutboxPayload(List<StoredFile> storedFileList) {
    public static final String EVENT_TYPE = "PRODUCT_VARIANT_IMAGES_DELETE";
}
