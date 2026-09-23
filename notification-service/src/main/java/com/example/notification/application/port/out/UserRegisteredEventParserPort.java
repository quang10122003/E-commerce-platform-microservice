package com.example.notification.application.port.out;

import com.example.notification.application.dto.event.UserRegisteredEvent;

// Cổng chuyển message auth thành event đăng ký người dùng.
public interface UserRegisteredEventParserPort {

    // Phân tích message nhận từ broker thành DTO application.
    UserRegisteredEvent parse(String message);
}
