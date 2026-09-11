package com.example.producr_service.application.strategy;

import com.example.producr_service.application.strategy.interfaces.IBucketUploadStrategy;
import com.example.producr_service.domain.until.PreparedUpload;
import com.example.producr_service.domain.until.StorageBucket;
import com.example.producr_service.domain.until.UploadFileCommand;

import java.util.UUID;

import static com.example.common.untill.FileUntils.getExtension;
import static com.example.common.untill.FileUntils.validateImage;


public class CategoryUploadStrategy implements IBucketUploadStrategy {
    @Override
    public StorageBucket supportedBucket() {
        return StorageBucket.CATEGORY;
    }

    @Override
    public PreparedUpload prepare(UploadFileCommand command) {
        validateImage(command.contentType());

        // Tạo tên file mới để tránh ghi đè file cũ.
        String objectPath ="Category"+UUID.randomUUID() + getExtension(command.fileName());

        return new PreparedUpload(
                supportedBucket(),
                objectPath,
                command.contentType(),
                command.content()
        );
    }
}
