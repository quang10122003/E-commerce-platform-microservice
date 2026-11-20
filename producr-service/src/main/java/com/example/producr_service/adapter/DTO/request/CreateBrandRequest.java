package com.example.producr_service.adapter.DTO.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateBrandRequest {

    @NotBlank(message = "Tên thương hiệu không được để trống")
    @Size(max = 255, message = "Tên thương hiệu tối đa 255 ký tự")
    private String name;
}
