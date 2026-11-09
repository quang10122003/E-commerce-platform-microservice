package com.example.notification.application.port.out.repo;

import com.example.notification.domain.model.Banner;
import com.example.notification.application.dto.response.AdminBannerResponse;
import java.util.List;
import java.util.Optional;

public interface BannerRepositoryPort {
    Banner save(Banner banner);
    Optional<Banner> findById(Long bannerId);
    void updateActive(Banner banner);
    long count();
    List<String> findActiveBannerHtml();
    void deleteById(Long bannerId);
    List<AdminBannerResponse> findAllAdminBanners();
}
