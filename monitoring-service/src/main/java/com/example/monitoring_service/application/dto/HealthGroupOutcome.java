package com.example.monitoring_service.application.dto;

/**
 * Kết quả gọi endpoint health group của một instance:
 * lấy được dữ liệu, group chưa được khai báo, hay không gọi được.
 */
public enum HealthGroupOutcome {
    /** Gọi thành công (HTTP 200 hoặc 503), có dữ liệu health để đọc. */
    AVAILABLE,
    /** Instance trả 404: health group chưa được cấu hình. */
    NOT_CONFIGURED,
    /** Không gọi được (timeout, lỗi kết nối, mã HTTP lạ hoặc body rỗng). */
    UNREACHABLE
}
