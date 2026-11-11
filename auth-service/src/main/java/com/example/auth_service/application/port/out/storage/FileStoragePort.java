package com.example.auth_service.application.port.out.storage;

import java.util.List;

public interface FileStoragePort {
    String upload(PreparedUpload upload);

    void delete(StorageBucket bucket, List<String> objectPaths);
}
