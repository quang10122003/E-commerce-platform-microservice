package com.example.auth_service.application.DTO.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreateShopRequestRequest(
        @NotBlank(message = "Tên shop không được để trống")
        @Size(max = 100, message = "Tên shop tối đa 100 ký tự")
        String shopName,
        @Size(max = 2000, message = "Mô tả shop tối đa 2000 ký tự")
        String shopDescription,
        @NotBlank(message = "Số điện thoại không được để trống")
        @Pattern(regexp = "^0\\d{9,10}$", message = "Số điện thoại phải gồm 10 hoặc 11 chữ số và bắt đầu bằng 0")
        String shopPhone,
        @NotNull(message = "Vui lòng chọn tỉnh/thành phố")
        @Positive(message = "Mã tỉnh/thành phố không hợp lệ")
        Integer provinceCode,
        @NotBlank(message = "Tỉnh/thành phố không được để trống")
        @Size(max = 100, message = "Tỉnh/thành phố tối đa 100 ký tự")
        String provinceName,
        @NotBlank(message = "Xã/phường không được để trống")
        @Size(max = 100, message = "Xã/phường tối đa 100 ký tự")
        String ward,
        @NotBlank(message = "Địa chỉ chi tiết không được để trống")
        @Size(max = 255, message = "Địa chỉ chi tiết tối đa 255 ký tự")
        String addressDetail) {
}
