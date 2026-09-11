package com.example.producr_service.domain.model;

import com.example.common.exception.BusinessException;
import com.example.producr_service.domain.error.DomainProductError;
import lombok.Getter;

import java.util.Objects;

@Getter
public class Brand {

    private final Long id;
    private final String name;
    public Brand(Long id, String name, String logoUrl) {
        if (name == null || name.isBlank()) {
            throw new BusinessException(DomainProductError.BRAND_NAME_REQUIRED);
        }
        this.id = id;
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Brand brand)) return false;
        return Objects.equals(id, brand.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
