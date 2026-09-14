package com.example.producr_service.application.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

// Mo ta 1 file trong "variantImages" thuoc ve variant/o anh nao trong request JSON
@Getter
@Setter
public class VariantImageMeta {

    @NotNull(message = "variantIndex khong duoc de trong")
    private Integer variantIndex; // vi tri variant trong mang "variants" cua request

    @NotNull(message = "imageIndex khong duoc de trong")
    private Integer imageIndex;   // vi tri anh trong mang "images" cua CHINH variant do
}