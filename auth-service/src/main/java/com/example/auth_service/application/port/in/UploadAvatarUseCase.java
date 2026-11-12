package com.example.auth_service.application.port.in;

import com.example.auth_service.application.DTO.command.UploadFileCommand;
import com.example.auth_service.application.DTO.repone.AvatarResponse;

public interface UploadAvatarUseCase {
    AvatarResponse uploadAvatar(String authorizationHeader, UploadFileCommand image);
}
