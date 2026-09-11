/**
*
*/
package com.example.producr_service.domain.until;
// Đây là dữ liệu đã được xử lý, kiểm tra và chuẩn hóa trước khi gửi đến storage
public record PreparedUpload(
        StorageBucket bucket,
        String objectPath,
        String contentType,
        byte[] content
) {
}