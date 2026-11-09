package com.example.notification.application.port.in;

import com.example.notification.application.dto.request.CreateBannerRequest;
import com.example.notification.application.dto.response.CreateBannerResponse;

public interface CreateBannerUseCase {
    CreateBannerResponse createBanner(CreateBannerRequest request);
}
