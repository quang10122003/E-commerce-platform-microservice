package com.example.producr_service.application.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class CreateProductRequest {

    @NotNull(message = "categoryId khong duoc de trong")
    private Long categoryId;

    private Long brandId; // co the null - san pham chua ro hang

    @NotBlank(message = "Ten san pham khong duoc de trong")
    private String name;

    private String description;
    private String imageUrl;

    // co the RONG neu san pham khong phan loai
    @Valid
    private List<AttributeRequest> attributes = new ArrayList<>();

    @NotEmpty(message = "San pham can it nhat 1 variant de ban")
    @Valid
    private List<VariantRequest> variants;

    // thuôc tính sản phẩm
    @Setter
    @Getter
    public static class AttributeRequest {
        @NotBlank(message = "Ten thuoc tinh khong duoc de trong")
        private String name;

        @NotEmpty(message = "Thuoc tinh can it nhat 1 gia tri")
        private List<@NotBlank String> values;

    }

    // 1 biên thể của sản phẩm
    @Setter
    @Getter
    public static class VariantRequest {

        @NotNull @PositiveOrZero(message = "Gia khong duoc am")
        private BigDecimal price;

        @PositiveOrZero(message = "Ton kho khong duoc am")
        private int stockQuantity;

        // RONG neu san pham khong phan loai (giong "bien the mac dinh")
        @Valid
        private List<AttributeSelection> attributeSelections = new ArrayList<>();

        @Valid
        private List<VariantImageRequest> images = new ArrayList<>();

    }


    //     Tro toi 1 gia tri thuoc tinh bang VI TRI trong mang "attributes"
    @Setter
    @Getter
    public static class AttributeSelection {
        @NotNull(message = "attributeIndex khong duoc de trong")
        private Integer attributeIndex;

        @NotNull(message = "valueIndex khong duoc de trong")
        private Integer valueIndex;

    }

    @Setter
    @Getter
    public static class VariantImageRequest {
        
        private String imageUrl;

        private boolean primary;

    }
}