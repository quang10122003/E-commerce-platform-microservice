package com.example.notification.domain.model;

import java.time.LocalDateTime;

public final class Banner {

    private final Long id;
    private final String nameBanner;
    private boolean active;
    private final String html;
    private final LocalDateTime createdAt;

    public Banner(Long id, String nameBanner, boolean active, String html, LocalDateTime createdAt) {
        this.id = id;
        this.nameBanner = nameBanner;
        this.active = active;
        this.html = html;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getNameBanner() {
        return nameBanner;
    }

    public boolean isActive() {
        return active;
    }

    public void updateActive(boolean active) {
        this.active = active;
    }

    public String getHtml() {
        return html;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
