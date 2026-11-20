package com.example.producr_service.application.dto.response;

import com.example.producr_service.domain.model.Brand;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BrandResponse {
    Long id;
    String name;
    int productCount;

    public static BrandResponse from(Brand brand) {
        return from(brand, 0);
    }

    public static BrandResponse from(Brand brand, long productCount) {
        BrandResponse response = new BrandResponse();
        response.id = brand.getId();
        response.name = brand.getName();
        response.productCount = Math.toIntExact(productCount);
        return response;
    }
}
