package com.example.producr_service.application.port.out;

import com.example.producr_service.domain.until.PreparedUpload;
import com.example.producr_service.domain.until.StorageBucket;

import java.util.List;

public interface FileStoragePort {
    String upload(PreparedUpload upload);

    // Xóa object khỏi bucket theo đúng đường dẫn đã lưu trên Storage.
    default void delete(StorageBucket bucket, String objectPath) {
        delete(bucket, List.of(objectPath));
    }

    // Xóa nhiều object trong cùng một bucket.
    void delete(StorageBucket bucket, List<String> objectPaths);
}
