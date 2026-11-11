package com.example.producr_service.application.dto.outbox;

import com.example.producr_service.application.port.out.storage.StoredFile;

import java.util.List;

public record StoredFilesDeleteOutboxPayload(List<StoredFile> files)  {
    // Giữ chuỗi cũ vì các event đã lưu trong outbox vẫn dùng giá trị này.
    public static final String EVENT_TYPE = "PRODUCT_IMAGES_DELETE";
}
