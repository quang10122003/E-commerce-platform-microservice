package com.example.producr_service.adapter.DTO.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
// Nhận toàn bộ thông tin sản phẩm và phân loại trong phần JSON của multipart.
public class UpdateProductRequest {
    @NotNull private Long categoryId;
    private Long brandId;
    @NotBlank private String name;
    private String description;
    @NotNull @Valid private List<@NotNull Attribute> attributes;
    @NotEmpty @Valid private List<@NotNull Variant> variants;

    @Getter @Setter
    public static class Attribute {
        private Long id;
        @NotBlank private String name;
        @NotEmpty @Valid private List<@NotNull Value> values;
    }

    @Getter @Setter
    public static class Value {
        private Long id;
        @NotBlank private String value;
    }

    @Getter @Setter
    public static class Variant {
        private Long id;
        private String sku;
        @NotNull @PositiveOrZero private BigDecimal price;
        @NotNull @PositiveOrZero private Integer stockQuantity;
        @NotNull @Valid private List<@NotNull Selection> attributeSelections;
        @NotNull @Valid private List<@NotNull Image> images;
    }

    @Getter @Setter
    public static class Selection {
        @NotNull private Integer attributeIndex;
        @NotNull private Integer valueIndex;
    }

    @Getter @Setter
    public static class Image {
        private Long id;
        private boolean primary;
    }
}
