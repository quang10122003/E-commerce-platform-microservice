package com.example.producr_service.application.strategy.interfaces;

import com.example.producr_service.application.dto.command.UploadFileCommand;
import com.example.producr_service.application.port.out.storage.PreparedUpload;
import com.example.producr_service.application.strategy.UploadPurpose;

public interface IUploadStrategy {

    UploadPurpose supportedPurpose();

    PreparedUpload prepare(UploadFileCommand command);
}
