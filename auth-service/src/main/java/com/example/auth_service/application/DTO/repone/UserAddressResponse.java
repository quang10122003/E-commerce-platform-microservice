package com.example.auth_service.application.DTO.repone;

import com.example.auth_service.domain.model.UserAddress;

public record UserAddressResponse(Long id, String recipientName, String province, String ward, String detail,
                                  String phone, boolean defaultAddress) {
    public static UserAddressResponse from(UserAddress address) {
        return new UserAddressResponse(address.getId(), address.getRecipientName(), address.getProvince(),
                address.getWard(), address.getDetail(), address.getPhone(), address.isDefaultAddress());
    }
}
