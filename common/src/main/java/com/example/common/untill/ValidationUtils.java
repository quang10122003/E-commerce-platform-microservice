package com.example.common.untill;

import java.util.Locale;

import com.example.common.error.ErrorCode;
import com.example.common.exception.BusinessException;
public final class ValidationUtils {
    private ValidationUtils() {
    }
    // Chuẩn hóa chuỗi: loại khoảng trắng 2 đầu và đổi chuỗi rỗng thành null.
    public static String normalize(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    // Chuẩn hóa chuỗi bắt buộc, nếu rỗng thì ném lỗi tương ứng.
    public static String requireNormalized(String value, ErrorCode errorCode) {
        String normalizedValue = normalize(value);
        if (normalizedValue == null) {
            throw new BusinessException(errorCode);
        }
        return normalizedValue;
    }

    // Chuẩn hóa email về dạng dùng chung trong hệ thống.
    public static String normalizeEmail(String email) {
        String normalizedEmail = normalize(email);
        return normalizedEmail == null ? null : normalizedEmail.toLowerCase(Locale.ROOT);
    }

    // Kiểm tra chuỗi có dữ liệu hợp lệ hay không.
    public static boolean hasText(String value) {
        return normalize(value) != null;
    }
}
