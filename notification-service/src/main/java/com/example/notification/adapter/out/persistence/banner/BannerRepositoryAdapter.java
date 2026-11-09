package com.example.notification.adapter.out.persistence.banner;

import com.example.notification.adapter.entity.BannerEntity;
import com.example.notification.adapter.mapper.BannerMapper;
import com.example.notification.application.port.out.repo.BannerRepositoryPort;
import com.example.notification.domain.model.Banner;
import com.example.notification.application.dto.response.AdminBannerResponse;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class BannerRepositoryAdapter implements BannerRepositoryPort {

    private final BannerJpa bannerJpa;
    private final BannerMapper bannerMapper;

    @Override
    public Optional<Banner> findById(Long bannerId) {
        return bannerJpa.findById(bannerId).map(bannerMapper::toDomain);
    }

    @Override
    public void updateActive(Banner banner) {
        bannerJpa.updateActive(banner.getId(), banner.isActive());
    }

    @Override
    public long count() {
        return bannerJpa.count();
    }

    @Override
    public List<String> findActiveBannerHtml() {
        return bannerJpa.findActiveBannerHtml();
    }

    @Override
    public void deleteById(Long bannerId) {
        bannerJpa.deleteById(bannerId);
    }

    @Override
    public List<AdminBannerResponse> findAllAdminBanners() {
        return bannerJpa.findAllAdminBanners().stream().map(bannerMapper::toAdminResponse).toList();
    }

    @Override
    public Banner save(Banner banner) {
        BannerEntity entity = bannerJpa.save(bannerMapper.toNewEntity(banner));
        return bannerMapper.toDomain(entity);
    }
}
