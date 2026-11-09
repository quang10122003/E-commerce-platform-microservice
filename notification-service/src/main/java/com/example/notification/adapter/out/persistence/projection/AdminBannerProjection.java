package com.example.notification.adapter.out.persistence.projection;

public interface AdminBannerProjection {
    Long getBannerId();
    String getName();
    String getHtml();
    boolean isActive();
}
