package com.example.producr_service.application.strategy.interfaces;

import com.example.producr_service.domain.until.PreparedUpload;
import com.example.producr_service.domain.until.StorageBucket;
import com.example.producr_service.domain.until.UploadFileCommand;

public interface IBucketUploadStrategy {

    StorageBucket supportedBucket();

    PreparedUpload prepare(UploadFileCommand command);
}
