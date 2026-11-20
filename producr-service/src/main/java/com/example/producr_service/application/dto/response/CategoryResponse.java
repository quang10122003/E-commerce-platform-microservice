package com.example.producr_service.application.dto.response;

import com.example.producr_service.domain.model.Category;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CategoryResponse {
    Long id;
    String name;
    String imageUrl;
    int productCount;

    public static CategoryResponse from(Category category) {
        return from(category, 0);
    }

    public static CategoryResponse from(Category category, long productCount) {
        CategoryResponse response = new CategoryResponse();
        response.id = category.getId();
        response.name = category.getName();
        response.imageUrl = category.getImageUrl();
        response.productCount = Math.toIntExact(productCount);
        return response;
    }
}
