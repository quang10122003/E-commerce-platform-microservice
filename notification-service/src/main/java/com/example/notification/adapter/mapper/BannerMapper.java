package com.example.notification.adapter.mapper;

import com.example.notification.adapter.entity.BannerEntity;
import com.example.notification.adapter.out.persistence.projection.AdminBannerProjection;
import com.example.notification.application.dto.response.AdminBannerResponse;
import com.example.notification.domain.model.Banner;
import org.springframework.stereotype.Component;

@Component
public class BannerMapper {

    public AdminBannerResponse toAdminResponse(AdminBannerProjection projection) {
        return new AdminBannerResponse(projection.getBannerId(), projection.getName(), projection.getHtml(), projection.isActive());
    }

    public BannerEntity toNewEntity(Banner banner) {
        return new BannerEntity(banner.getNameBanner(), banner.isActive(), banner.getHtml());
    }

    public Banner toDomain(BannerEntity entity) {
        return new Banner(entity.getId(), entity.getNameBanner(), entity.isActive(),
                entity.getHtml(), entity.getCreatedAt());
    }
}
