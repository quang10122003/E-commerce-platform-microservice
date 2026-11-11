package com.example.auth_service.application.port.in;

import com.example.auth_service.application.DTO.command.UploadFileCommand;
import com.example.auth_service.application.DTO.repone.ShopLogoResponse;

public interface UploadShopLogoUseCase {
    ShopLogoResponse uploadShopLogo(String authorizationHeader, UploadFileCommand image);
}
