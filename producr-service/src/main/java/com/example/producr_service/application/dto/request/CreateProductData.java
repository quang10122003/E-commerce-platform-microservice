package com.example.producr_service.application.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

// Dữ liệu nội bộ tạo sản phẩm: nhận thông tin từ HTTP mapper và bổ sung metadata ảnh sau upload.
@Setter
@Getter
public class CreateProductData {
    private Long categoryId;
    private Long brandId;
    private String name;
    private String description;
    // URL ảnh bìa do backend gán sau khi upload.
    private String imageUrl;
    // Đường dẫn ảnh bìa do backend gán sau khi upload.
    private String objectPath;
    // Danh sách thuộc tính dùng để xác định các lựa chọn của variant.
    private List<AttributeRequest> attributes = new ArrayList<>();
    private List<VariantRequest> variants;

    @Setter
    @Getter
    public static class AttributeRequest {
        private String name;
        private List<String> values;
    }

    @Setter
    @Getter
    public static class VariantRequest {
        private BigDecimal price;
        private int stockQuantity;
        private List<AttributeSelection> attributeSelections = new ArrayList<>();
        // Giữ thứ tự slot ảnh để ghép đúng file upload theo variantIndex và imageIndex.
        private List<VariantImageRequest> images = new ArrayList<>();
    }

    @Setter
    @Getter
    public static class AttributeSelection {
        private Integer attributeIndex;
        private Integer valueIndex;
    }

    @Setter
    @Getter
    public static class VariantImageRequest {
        // URL ảnh phân loại do backend gán sau khi upload.
        private String imageUrl;
        // Đường dẫn ảnh phân loại do backend gán sau khi upload.
        private String objectPath;
        private boolean primary;
    }
}
