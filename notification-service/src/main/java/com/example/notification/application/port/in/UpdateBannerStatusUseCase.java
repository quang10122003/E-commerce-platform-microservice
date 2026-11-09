package com.example.notification.application.port.in;

import com.example.notification.application.dto.request.UpdateBannerStatusRequest;

public interface UpdateBannerStatusUseCase {
    void updateBannerStatus(Long bannerId, UpdateBannerStatusRequest request);
}
