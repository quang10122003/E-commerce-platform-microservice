package com.example.producr_service.application.dto.response;

// Kết quả kiểm tra sản phẩm còn tồn tại trong DB, không phụ thuộc trạng thái bán.
public record ProductExistsResponse(boolean exists) {}
