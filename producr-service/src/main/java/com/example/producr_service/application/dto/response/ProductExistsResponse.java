package com.example.producr_service.application.dto.response;

// Ngừng bán không đồng nghĩa đã xóa, nên kết quả tồn tại không phụ thuộc trạng thái bán.
public record ProductExistsResponse(boolean exists) {}
