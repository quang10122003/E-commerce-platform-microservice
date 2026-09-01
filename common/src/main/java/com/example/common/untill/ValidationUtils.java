package com.example.common.untill;

import java.util.Locale;

import org.springframework.stereotype.Component;

import com.example.common.error.ErrorCode;
import com.example.common.exception.ApplicationException;
@Component
public class ValidationUtils {
    // Chuẩn hóa chuỗi: loại khoảng trắng 2 đầu và đổi chuỗi rỗng thành null.
    public String normalize(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    // Chuẩn hóa chuỗi bắt buộc, nếu rỗng thì ném lỗi tương ứng.
    public String requireNormalized(String value, ErrorCode errorCode) {
        String normalizedValue = normalize(value);
        if (normalizedValue == null) {
            throw new ApplicationException(errorCode);
        }
        return normalizedValue;
    }

    // Chuẩn hóa email về dạng dùng chung trong hệ thống.
    public String normalizeEmail(String email) {
        String normalizedEmail = normalize(email);
        return normalizedEmail == null ? null : normalizedEmail.toLowerCase(Locale.ROOT);
    }

    // Kiểm tra chuỗi có dữ liệu hợp lệ hay không.
    public boolean hasText(String value) {
        return normalize(value) != null;
    }
}
