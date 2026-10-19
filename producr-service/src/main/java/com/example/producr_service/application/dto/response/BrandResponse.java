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

    public static BrandResponse from(Brand brand) {
        BrandResponse response = new BrandResponse();
        response.id = brand.getId();
        response.name = brand.getName();
        return response;
    }
}
