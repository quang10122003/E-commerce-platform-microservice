package com.example.order_service.application.port.out.storage;

import java.util.List;

public interface FileStoragePort {
    // Upload file và trả về URL công khai.
    String upload(StorageBucket bucket, String objectPath, String contentType, byte[] content);

    void delete(StorageBucket bucket, List<String> objectPaths);
}
