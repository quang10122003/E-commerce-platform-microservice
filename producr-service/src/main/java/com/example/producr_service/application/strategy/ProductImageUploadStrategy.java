package com.example.producr_service.application.strategy;

import com.example.common.untill.FileUntils;
import com.example.producr_service.application.strategy.interfaces.IBucketUploadStrategy;
import com.example.producr_service.domain.until.PreparedUpload;
import com.example.producr_service.domain.until.StorageBucket;
import com.example.producr_service.domain.until.UploadFileCommand;

import java.util.UUID;

import static com.example.common.untill.FileUntils.getExtension;

public class ProductImageUploadStrategy  implements IBucketUploadStrategy {
    @Override
    public StorageBucket supportedBucket() {
        return StorageBucket.PRODUCT_IMAGES;
    }

    @Override
    public PreparedUpload prepare(UploadFileCommand command) {
        FileUntils.validateImage(command.contentType());
        // Tạo tên file mới để tránh ghi đè file cũ.
        String objectPath ="Product"+ UUID.randomUUID() + getExtension(command.fileName());

        return new PreparedUpload(
                supportedBucket(),
                objectPath,
                command.contentType(),
                command.content()
        );
    }
}
