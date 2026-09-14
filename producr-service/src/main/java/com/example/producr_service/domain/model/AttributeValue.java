package com.example.producr_service.domain.model;

import com.example.common.exception.BusinessException;
import com.example.producr_service.domain.error.DomainProductError;
import lombok.Getter;

import java.util.Objects;


@Getter
public class AttributeValue {

    private final Long id;
    private String value;

    public AttributeValue(Long id, String value) {
        if (value == null || value.isBlank()) {
            throw new BusinessException(DomainProductError.ATTRIBUTE_VALUE_NAME_REQUIRED);
        }
        this.id = id;
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AttributeValue that)) return false;
        if (id == null || that.id == null) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return id == null ? System.identityHashCode(this) : Objects.hash(id);
    }
}
