package com.example.producr_service.application.strategy;

import com.example.common.untill.FileUntils;
import com.example.producr_service.application.dto.request.UploadFileCommand;
import com.example.producr_service.application.port.out.storage.PreparedUpload;
import com.example.producr_service.application.port.out.storage.StorageBucket;
import com.example.producr_service.application.strategy.interfaces.IBucketUploadStrategy;

import java.util.UUID;

import static com.example.common.untill.FileUntils.getExtension;

// Chuẩn hóa file ảnh variant trước khi gửi đến storage adapter.
public class ProductVariantsStrategy implements IBucketUploadStrategy {
    @Override
    public StorageBucket supportedBucket() {
        return StorageBucket.PRODUCT_VARIANTS;
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
