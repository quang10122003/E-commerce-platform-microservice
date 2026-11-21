package com.example.auth_service.domain.model;

public class UserAddress {
    private Long id;
    private Long userId;
    private String recipientName;
    private String province;
    private String ward;
    private String detail;
    private String phone;
    private boolean defaultAddress;

    public UserAddress(Long id, Long userId, String recipientName, String province, String ward, String detail,
                       String phone, boolean defaultAddress) {
        this.id = id;
        this.userId = userId;
        this.recipientName = recipientName;
        this.province = province;
        this.ward = ward;
        this.detail = detail;
        this.phone = phone;
        this.defaultAddress = defaultAddress;
    }

    // ID do persistence sinh khi lưu.
    public static UserAddress create(Long userId, String recipientName, String province, String ward, String detail,
                                     String phone, boolean defaultAddress) {
        return new UserAddress(null, userId, recipientName.trim(), province.trim(), ward.trim(), detail.trim(),
                phone.trim(), defaultAddress);
    }

    public void markDefault() {
        defaultAddress = true;
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public String getRecipientName() {
        return recipientName;
    }

    public String getProvince() {
        return province;
    }

    public String getWard() {
        return ward;
    }

    public String getDetail() {
        return detail;
    }

    public String getPhone() {
        return phone;
    }

    public boolean isDefaultAddress() {
        return defaultAddress;
    }
}
