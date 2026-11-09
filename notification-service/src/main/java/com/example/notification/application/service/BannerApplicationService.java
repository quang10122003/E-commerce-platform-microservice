package com.example.notification.application.service;

import com.example.notification.application.dto.request.CreateBannerRequest;
import com.example.notification.application.dto.response.CreateBannerResponse;
import com.example.notification.application.port.in.CreateBannerUseCase;
import com.example.notification.application.port.in.UpdateBannerStatusUseCase;
import com.example.notification.application.dto.request.UpdateBannerStatusRequest;
import com.example.notification.application.port.in.GetActiveBannersUseCase;
import com.example.notification.application.port.in.DeleteBannerUseCase;
import com.example.notification.application.port.in.GetAdminBannersUseCase;
import com.example.notification.application.dto.response.AdminBannerResponse;
import java.util.List;
import com.example.notification.application.port.out.repo.BannerRepositoryPort;
import com.example.notification.domain.model.Banner;
import com.example.common.exception.BusinessException;
import com.example.notification.application.error.NotificationError;
import org.springframework.transaction.annotation.Isolation;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
public class BannerApplicationService implements CreateBannerUseCase, GetAdminBannersUseCase, DeleteBannerUseCase,
        GetActiveBannersUseCase, UpdateBannerStatusUseCase {

    private final BannerRepositoryPort bannerRepositoryPort;

    @Override
    @Transactional
    public void updateBannerStatus(Long bannerId, UpdateBannerStatusRequest request) {
        Banner banner = bannerRepositoryPort.findById(bannerId)
                .orElseThrow(() -> new BusinessException(NotificationError.BANNER_NOT_FOUND));
        banner.updateActive(request.active());
        bannerRepositoryPort.updateActive(banner);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getActiveBannerHtml() {
        return bannerRepositoryPort.findActiveBannerHtml();
    }

    @Override
    @Transactional
    public void deleteBanner(Long bannerId) {
        bannerRepositoryPort.deleteById(bannerId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminBannerResponse> getAllBanners() {
        return bannerRepositoryPort.findAllAdminBanners();
    }

    @Override
    // Ngăn các request tạo đồng thời cùng vượt qua giới hạn số banner.
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public CreateBannerResponse createBanner(CreateBannerRequest request) {
        if (bannerRepositoryPort.count() >= 5) {
            throw new BusinessException(NotificationError.BANNER_LIMIT_REACHED);
        }
        Banner banner = new Banner(null, request.nameBanner(), true, request.html(), null);
        Banner savedBanner = bannerRepositoryPort.save(banner);
        return new CreateBannerResponse(savedBanner.getId());
    }
}
