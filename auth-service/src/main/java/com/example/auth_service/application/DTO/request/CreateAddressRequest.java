package com.example.auth_service.application.DTO.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

// defaultAddress bỏ trống được xem là false; địa chỉ đầu tiên luôn thành mặc định.
public record CreateAddressRequest(
        @NotBlank(message = "Tên người nhận không được để trống")
        @Size(max = 50, message = "Tên người nhận tối đa 50 ký tự")
        String recipientName,
        @NotBlank(message = "Tỉnh/thành phố không được để trống")
        @Size(max = 100, message = "Tỉnh/thành phố tối đa 100 ký tự")
        String province,
        @NotBlank(message = "Xã/phường không được để trống")
        @Size(max = 100, message = "Xã/phường tối đa 100 ký tự")
        String ward,
        @NotBlank(message = "Địa chỉ chi tiết không được để trống")
        @Size(max = 255, message = "Địa chỉ chi tiết tối đa 255 ký tự")
        String detail,
        @NotBlank(message = "Số điện thoại không được để trống")
        @Pattern(regexp = "^0\\d{9,10}$", message = "Số điện thoại phải gồm 10 hoặc 11 chữ số và bắt đầu bằng 0")
        String phone,
        Boolean defaultAddress) {
}
