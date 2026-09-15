package com.example.producr_service.application.strategy;

import com.example.producr_service.application.dto.command.UploadFileCommand;
import com.example.producr_service.application.port.out.storage.PreparedUpload;
import com.example.producr_service.application.port.out.storage.StorageBucket;
import com.example.producr_service.application.strategy.interfaces.IUploadStrategy;

import java.util.UUID;

import static com.example.common.untill.FileUntils.getExtension;
import static com.example.common.untill.FileUntils.validateImage;

// Chứa luồng xử lý chung cho các strategy upload ảnh.
public abstract class AbstractImageUploadStrategy implements IUploadStrategy {
    private final UploadPurpose uploadPurpose;
    private final StorageBucket storageBucket;
    private final String objectPrefix;

    protected AbstractImageUploadStrategy(
            UploadPurpose uploadPurpose,
            StorageBucket storageBucket,
            String objectPrefix
    ) {
        this.uploadPurpose = uploadPurpose;
        this.storageBucket = storageBucket;
        this.objectPrefix = objectPrefix;
    }

    @Override
    public UploadPurpose supportedPurpose() {
        return uploadPurpose;
    }

    @Override
    public PreparedUpload prepare(UploadFileCommand command) {
        validateImage(command.contentType());

        // Tạo tên file mới để tránh ghi đè file cũ.
        String objectPath = objectPrefix + UUID.randomUUID() + getExtension(command.fileName());

        return new PreparedUpload(
                storageBucket,
                objectPath,
                command.contentType(),
                command.content()
        );
    }
}
