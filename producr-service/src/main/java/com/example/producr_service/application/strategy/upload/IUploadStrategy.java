package com.example.producr_service.application.strategy.upload;

import com.example.producr_service.application.dto.command.UploadFileCommand;
import com.example.producr_service.application.port.out.storage.PreparedUpload;

// Định nghĩa strategy chuẩn bị file theo từng mục đích upload.
public interface IUploadStrategy {

    // Trả về mục đích upload mà strategy phụ trách.
    UploadPurpose supportedPurpose();

    // Chuẩn hóa và chuẩn bị file trước khi gửi đến storage.
    PreparedUpload prepare(UploadFileCommand command);
}
