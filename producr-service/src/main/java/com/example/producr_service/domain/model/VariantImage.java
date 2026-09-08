package com.example.producr_service.domain.model;
import java.util.Objects;

public class VariantImage {

    private final Long id;
    private String imageUrl;
    private boolean primary;

    public VariantImage(Long id, String imageUrl, boolean primary, int sortOrder) {
        if (imageUrl == null || imageUrl.isBlank()) {
            throw new IllegalArgumentException("Duong dan anh khong duoc rong");
        }
        this.id = id;
        this.imageUrl = imageUrl;
        this.primary = primary;
    }

    public Long getId() {
        return id;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public boolean isPrimary() {
        return primary;
    }

    public void markAsPrimary() {
        this.primary = true;
    }

    public void unmarkPrimary() {
        this.primary = false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VariantImage that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
