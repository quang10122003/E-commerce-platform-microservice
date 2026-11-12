package com.example.order_service.domain.model;

import com.example.common.exception.BusinessException;
import com.example.order_service.domain.error.ReviewDomainError;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

// Định dạng ảnh đánh giá được nhận; đối chiếu cả content type lẫn chữ ký đầu file vì content type do client tự khai.
public enum ReviewImageFormat {
    JPEG("image/jpeg", ".jpg"),
    PNG("image/png", ".png"),
    WEBP("image/webp", ".webp");

    public static final long MAX_SIZE_BYTES = 15L * 1024 * 1024;

    private final String contentType;
    private final String extension;

    ReviewImageFormat(String contentType, String extension) {
        this.contentType = contentType;
        this.extension = extension;
    }

    // Xác định định dạng ảnh từ content type và nội dung file; ảnh rỗng, quá 15MB hoặc không khớp định dạng nào thì báo lỗi.
    public static ReviewImageFormat detect(String contentType, byte[] content) {
        if (content == null || content.length == 0 || content.length > MAX_SIZE_BYTES) {
            throw new BusinessException(ReviewDomainError.INVALID_IMAGE);
        }
        for (ReviewImageFormat format : values()) {
            if (format.contentType.equalsIgnoreCase(contentType) && format.matchesSignature(content)) {
                return format;
            }
        }
        throw new BusinessException(ReviewDomainError.INVALID_IMAGE);
    }

    // Kiểm tra mấy byte đầu file có đúng chữ ký của định dạng này không.
    private boolean matchesSignature(byte[] content) {
        return switch (this) {
            case JPEG -> startsWith(content, 0, new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF});
            case PNG -> startsWith(content, 0, new byte[]{(byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1A, '\n'});
            case WEBP -> startsWith(content, 0, "RIFF".getBytes(StandardCharsets.US_ASCII))
                    && startsWith(content, 8, "WEBP".getBytes(StandardCharsets.US_ASCII));
        };
    }

    // So khớp một đoạn byte của file tại vị trí offset với chữ ký cho trước.
    private static boolean startsWith(byte[] content, int offset, byte[] signature) {
        return content.length >= offset + signature.length
                && Arrays.equals(content, offset, offset + signature.length, signature, 0, signature.length);
    }

    public String getContentType() {
        return contentType;
    }

    public String getExtension() {
        return extension;
    }
}
