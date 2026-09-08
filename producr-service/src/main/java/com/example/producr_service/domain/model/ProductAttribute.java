package com.example.producr_service.domain.model;


import com.example.common.exception.BusinessException;
import com.example.producr_service.domain.error.DomainProductError;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;


@Getter
public class ProductAttribute {

    private final Long id;
    private final String name;
    private final List<AttributeValue> values = new ArrayList<>();

    public ProductAttribute(Long id, String name) {
        if (name == null || name.isBlank()) {
            throw new BusinessException(DomainProductError.PRODUCT_ATTRIBUTE_NAME_REQUIRED);
        }
        this.id = id;
        this.name = name;
    }


    public List<AttributeValue> getValues() {
        return Collections.unmodifiableList(values);
    }

    public void addValue(AttributeValue value) {
        values.add(value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProductAttribute that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
