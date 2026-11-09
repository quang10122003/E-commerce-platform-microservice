package com.example.notification.adapter.out.persistence.banner;

import com.example.notification.adapter.entity.BannerEntity;
import com.example.notification.adapter.out.persistence.projection.AdminBannerProjection;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BannerJpa extends JpaRepository<BannerEntity, Long> {
    @Modifying
    @Query("update BannerEntity b set b.active = :active where b.id = :bannerId")
    void updateActive(@Param("bannerId") Long bannerId, @Param("active") boolean active);

    @Query("select b.html from BannerEntity b where b.active = true order by b.id desc")
    List<String> findActiveBannerHtml();

    @Query("select b.id as bannerId, b.nameBanner as name, b.html as html, b.active as active from BannerEntity b order by b.id desc")
    List<AdminBannerProjection> findAllAdminBanners();
}
