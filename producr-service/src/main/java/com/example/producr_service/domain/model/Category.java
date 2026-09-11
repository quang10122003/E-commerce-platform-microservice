package com.example.producr_service.domain.model;

import com.example.common.exception.BusinessException;
import com.example.producr_service.domain.error.DomainProductError;
import lombok.Getter;

import java.util.Objects;

@Getter
public class Category {

    private final Long id;
    private String name;
    private  String imageUrl;

    public Category(Long id, String name,String imageUrl) {
        if (name == null || name.isBlank()) {
            throw new BusinessException(DomainProductError.CATEGORY_NAME_REQUIRED);
        }
        this.id = id;
        this.name = name;
        this.imageUrl = imageUrl;
    }

    public void rename(String newName) {
        if (newName == null || newName.isBlank()) {
            throw new BusinessException(DomainProductError.CATEGORY_NAME_REQUIRED);
        }
        this.name = newName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Category category)) return false;
        return Objects.equals(id, category.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
