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
    // Mã định danh category trả về cho client.
    Long id;
    // Tên category trả về cho client.
    String name;
    // Đường dẫn ảnh category trả về cho client.
    String imageUrl;

    // Chuyển domain Category thành dữ liệu trả về cho API.
    public static CategoryResponse from(Category category) {
        CategoryResponse response = new CategoryResponse();
        response.id = category.getId();
        response.name = category.getName();
        response.imageUrl = category.getImageUrl();
        return response;
    }
}
