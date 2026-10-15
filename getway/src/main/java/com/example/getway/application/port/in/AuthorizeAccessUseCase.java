package com.example.getway.application.port.in;

// Boundary xác thực quyền truy cập để inbound filter không phụ thuộc implementation.
public interface AuthorizeAccessUseCase {
    // Kiểm tra token và trạng thái truy cập hiện tại.
    void authorize(String token);
}
