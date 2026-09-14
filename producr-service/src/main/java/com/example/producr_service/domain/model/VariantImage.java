package com.example.producr_service.domain.model;

import com.example.common.exception.BusinessException;
import com.example.producr_service.domain.error.DomainProductError;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;
@Getter
@AllArgsConstructor
public class VariantImage {

    private final Long id;
    private String imageUrl;
    private boolean primary;

    public VariantImage(Long id, String imageUrl, boolean primary, int sortOrder) {
        if (imageUrl == null || imageUrl.isBlank()) {
            throw new BusinessException(DomainProductError.IMAGE_URL_REQUIRED);
        }
        this.id = id;
        this.imageUrl = imageUrl;
        this.primary = primary;
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
