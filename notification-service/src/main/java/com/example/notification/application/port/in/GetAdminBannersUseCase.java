package com.example.notification.application.port.in;

import com.example.notification.application.dto.response.AdminBannerResponse;
import java.util.List;

public interface GetAdminBannersUseCase {
    List<AdminBannerResponse> getAllBanners();
}
