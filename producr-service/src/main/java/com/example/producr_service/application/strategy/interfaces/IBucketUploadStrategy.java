package com.example.producr_service.application.strategy.interfaces;

import com.example.producr_service.application.port.out.storage.PreparedUpload;
import com.example.producr_service.application.port.out.storage.StorageBucket;
import com.example.producr_service.application.dto.request.UploadFileCommand;

public interface IBucketUploadStrategy {

    StorageBucket supportedBucket();

    PreparedUpload prepare(UploadFileCommand command);
}

